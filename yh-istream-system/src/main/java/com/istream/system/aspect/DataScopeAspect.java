package com.istream.system.aspect;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.istream.common.annotation.DataScope;
import com.istream.common.constant.Constants;
import com.istream.common.enums.DataScopeEnum;
import com.istream.common.enums.StatusEnum;
import com.istream.common.model.query.BaseQuery;
import com.istream.framework.cache.CacheService;
import com.istream.framework.security.SecurityUtils;
import com.istream.system.entity.SysDept;
import com.istream.system.entity.SysRole;
import com.istream.system.entity.SysUser;
import com.istream.system.entity.SysUserRole;
import com.istream.system.mapper.SysDeptMapper;
import com.istream.system.mapper.SysRoleDeptMapper;
import com.istream.system.mapper.SysRoleMapper;
import com.istream.system.mapper.SysUserMapper;
import com.istream.system.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 数据权限 AOP 切面
 *
 * <p>拦截 {@link DataScope} 注解的方法，根据当前用户角色动态拼接数据权限 SQL 条件，
 * 注入到 {@link BaseQuery#getParams()} 中。</p>
 *
 * <p>别名白名单校验：仅允许 {@link #ALLOWED_ALIASES} 中的别名参与 SQL 拼接，
 * 防止注解参数被恶意构造导致 SQL 注入。</p>
 *
 * @author istream
 * @since 2026-08-17
 */
@Slf4j
@Aspect
@Component
@Order(1)
@RequiredArgsConstructor
public class DataScopeAspect {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleDeptMapper sysRoleDeptMapper;
    private final SysDeptMapper sysDeptMapper;
    private final CacheService cacheService;

    private static final Duration DATA_SCOPE_CACHE_TTL = Duration.ofMinutes(10);

    /**
     * SQL 别名白名单，仅允许这些别名参与数据权限 SQL 拼接
     */
    private static final Set<String> ALLOWED_ALIASES = Set.of("d", "u", "dept", "user", "t", "a", "b");

    private static final Pattern ALIAS_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]{0,30}$");

    private static final Pattern ANCESTORS_PATTERN = Pattern.compile("^[0-9,]*$");

    /**
     * 用户数据权限缓存对象（可序列化存入 Redis）
     */
    private record UserDataScopeCache(
            Set<Long> allAccessRoleIds,
            Set<Long> deptAndChildRoleIds,
            Set<Long> deptRoleIds,
            Map<Long, List<Long>> customDeptIdsByRole,
            Set<Long> selfRoleIds,
            Long userDeptId,
            String userDeptAncestors
    ) {
    }

    /**
     * 环绕通知：注入数据权限 SQL 条件
     *
     * @param point     切点
     * @param dataScope 数据权限注解
     * @return 方法执行结果
     * @throws Throwable 方法执行异常
     */
    @Around("@annotation(dataScope)")
    public Object around(ProceedingJoinPoint point, DataScope dataScope) throws Throwable {
        Long userId;
        try {
            userId = SecurityUtils.getLoginUserId();
        } catch (Exception e) {
            log.debug("数据权限处理跳过（非登录态）", e);
            return point.proceed();
        }
        if (userId == null) {
            return point.proceed();
        }

        String deptAlias = validateAlias(dataScope.deptAlias(), "deptAlias");
        String userAlias = validateAlias(dataScope.userAlias(), "userAlias");

        String sql = buildDataScopeSql(userId, deptAlias, userAlias);
        if (sql != null) {
            injectParams(point.getArgs(), sql);
        }
        return point.proceed();
    }

    /**
     * 校验 SQL 别名是否合法
     */
    private String validateAlias(String alias, String paramName) {
        if (alias == null || alias.isBlank()) {
            throw new IllegalArgumentException("DataScope " + paramName + " 不能为空");
        }
        if (!ALLOWED_ALIASES.contains(alias) && !ALIAS_PATTERN.matcher(alias).matches()) {
            log.error("DataScope {} 包含非法别名: {}", paramName, alias);
            throw new IllegalArgumentException("DataScope " + paramName + " 包含非法字符: " + alias);
        }
        return alias;
    }

    /**
     * 构建数据权限 SQL（优先从缓存读取，缓存未命中时查询数据库并回填）
     */
    private String buildDataScopeSql(Long userId, String deptAlias, String userAlias) {
        UserDataScopeCache cache = getOrLoadCache(userId);
        if (cache == null) {
            return null;
        }

        // 拥有全部数据权限的角色 → 不加任何限制
        if (!cache.allAccessRoleIds.isEmpty()) {
            return null;
        }

        StringJoiner sqlJoiner = new StringJoiner(" OR ");

        // DEPT_AND_CHILD：本级 + 所有子孙部门
        for (Long roleId : cache.deptAndChildRoleIds) {
            appendDeptAndChildScopeCached(sqlJoiner, cache, deptAlias);
        }
        // DEPT：仅本部门
        for (Long roleId : cache.deptRoleIds) {
            appendDeptScope(sqlJoiner, cache.userDeptId, deptAlias);
        }
        // CUSTOM：指定部门
        for (Long roleId : cache.customDeptIdsByRole.keySet()) {
            List<Long> deptIds = cache.customDeptIdsByRole.get(roleId);
            if (deptIds != null && !deptIds.isEmpty()) {
                String inClause = deptIds.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(",", "(", ")"));
                sqlJoiner.add(deptAlias + ".id IN " + inClause);
            }
        }
        // SELF：仅本人数据
        if (!cache.selfRoleIds.isEmpty()) {
            sqlJoiner.add(userAlias + ".id = " + userId);
        }

        String scopeSql = sqlJoiner.toString();
        if (scopeSql.isBlank()) {
            return null;
        }
        return " AND (" + scopeSql + ")";
    }

    /**
     * 从缓存或数据库加载用户数据权限信息
     */
    private UserDataScopeCache getOrLoadCache(Long userId) {
        String cacheKey = Constants.DATA_SCOPE_CACHE_PREFIX + userId;
        UserDataScopeCache cache = cacheService.get(cacheKey);
        if (cache != null) {
            return cache;
        }

        cache = loadFromDb(userId);
        if (cache != null) {
            cacheService.set(cacheKey, cache, DATA_SCOPE_CACHE_TTL);
        }
        return cache;
    }

    /**
     * 从数据库加载用户数据权限信息（缓存未命中时调用）
     *
     * @param userId 用户ID
     * @return 用户数据权限缓存对象，用户不存在或无角色时返回 null
     */
    private UserDataScopeCache loadFromDb(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            return null;
        }

        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (userRoles.isEmpty()) {
            return null;
        }

        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).toList();
        List<SysRole> roles = sysRoleMapper.selectByIds(roleIds);
        Map<Long, SysRole> roleMap = roles.stream()
                .filter(r -> r != null && !Objects.equals(r.getStatus(), StatusEnum.DISABLED.getCode()))
                .collect(Collectors.toMap(SysRole::getId, r -> r));

        if (roleMap.isEmpty()) {
            return null;
        }

        Set<Long> allAccessRoleIds = new HashSet<>();
        Set<Long> deptAndChildRoleIds = new HashSet<>();
        Set<Long> deptRoleIds = new HashSet<>();
        Map<Long, List<Long>> customDeptIdsByRole = new HashMap<>();
        Set<Long> selfRoleIds = new HashSet<>();

        for (SysUserRole ur : userRoles) {
            SysRole role = roleMap.get(ur.getRoleId());
            if (role == null) {
                continue;
            }
            DataScopeEnum scope = DataScopeEnum.of(role.getDataScope());
            switch (scope) {
                case ALL -> allAccessRoleIds.add(role.getId());
                case DEPT_AND_CHILD -> deptAndChildRoleIds.add(role.getId());
                case DEPT -> deptRoleIds.add(role.getId());
                case CUSTOM -> {
                    List<Long> deptIds = sysRoleDeptMapper.selectDeptIdsByRoleId(role.getId());
                    if (!deptIds.isEmpty()) {
                        customDeptIdsByRole.put(role.getId(), deptIds);
                    }
                }
                case SELF -> selfRoleIds.add(role.getId());
            }
        }

        String userDeptAncestors = null;
        if (!deptAndChildRoleIds.isEmpty() && user.getDeptId() != null) {
            SysDept dept = sysDeptMapper.selectById(user.getDeptId());
            if (dept != null) {
                String ancestors = dept.getAncestors();
                if (ancestors != null && ANCESTORS_PATTERN.matcher(ancestors).matches()) {
                    userDeptAncestors = ancestors;
                }
            }
        }

        return new UserDataScopeCache(allAccessRoleIds, deptAndChildRoleIds,
                deptRoleIds, customDeptIdsByRole, selfRoleIds,
                user.getDeptId(), userDeptAncestors);
    }

    /**
     * 清除指定用户的数据权限缓存
     *
     * @param userId 用户ID
     * @since 2026-09-12
     */
    public void evictCache(Long userId) {
        cacheService.delete(Constants.DATA_SCOPE_CACHE_PREFIX + userId);
    }

    private void appendDeptAndChildScopeCached(StringJoiner sqlJoiner, UserDataScopeCache cache, String deptAlias) {
        Long deptId = cache.userDeptId;
        if (deptId == null) {
            return;
        }
        if (cache.userDeptAncestors != null && !cache.userDeptAncestors.isBlank()) {
            sqlJoiner.add(deptAlias
                    + ".id IN (SELECT id FROM sys_dept WHERE FIND_IN_SET(" + deptId + ", ancestors) > 0"
                    + " OR id = " + deptId + ")");
        } else {
            sqlJoiner.add(deptAlias + ".id = " + deptId);
        }
    }

    private void appendDeptScope(StringJoiner sqlJoiner, Long deptId, String deptAlias) {
        if (deptId != null) {
            sqlJoiner.add(deptAlias + ".id = " + deptId);
        }
    }

    private void injectParams(Object[] args, String sql) {
        for (Object arg : args) {
            if (arg instanceof BaseQuery query) {
                query.getParams().put(DataScope.DATA_SCOPE_KEY, sql);
                return;
            }
        }
        log.warn("DataScope 注解生效但方法参数中未找到 BaseQuery，数据权限SQL被丢弃: {}", sql);
    }
}