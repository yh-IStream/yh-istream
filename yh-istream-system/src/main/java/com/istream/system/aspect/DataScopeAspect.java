package com.istream.system.aspect;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.istream.common.annotation.DataScope;
import com.istream.common.enums.DataScopeEnum;
import com.istream.common.enums.StatusEnum;
import com.istream.common.model.query.BaseQuery;
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
 * 注入到 {@link BaseQuery#params} 中。</p>
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

    /**
     * SQL 别名白名单，仅允许这些别名参与数据权限 SQL 拼接
     */
    private static final Set<String> ALLOWED_ALIASES = Set.of("d", "u", "dept", "user", "t", "a", "b");

    private static final Pattern ALIAS_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]{0,30}$");

    private static final Pattern ANCESTORS_PATTERN = Pattern.compile("^[0-9,]*$");

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
            log.debug("数据权限处理跳过（非登录态）: {}", e.getMessage());
            return point.proceed();
        }
        if (userId == null) {
            return point.proceed();
        }

        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            return point.proceed();
        }

        String deptAlias = validateAlias(dataScope.deptAlias(), "deptAlias");
        String userAlias = validateAlias(dataScope.userAlias(), "userAlias");

        String sql = buildDataScopeSql(user, deptAlias, userAlias);
        if (sql != null) {
            injectParams(point.getArgs(), sql);
        }
        return point.proceed();
    }

    /**
     * 校验 SQL 别名是否合法
     *
     * @param alias     待校验别名
     * @param paramName 参数名称（用于日志）
     * @return 校验通过的别名
     */
    private String validateAlias(String alias, String paramName) {
        if (alias == null || alias.isEmpty()) {
            throw new IllegalArgumentException("DataScope " + paramName + " 不能为空");
        }
        if (!ALLOWED_ALIASES.contains(alias) && !ALIAS_PATTERN.matcher(alias).matches()) {
            log.error("DataScope {} 包含非法别名: {}", paramName, alias);
            throw new IllegalArgumentException("DataScope " + paramName + " 包含非法字符: " + alias);
        }
        return alias;
    }

    private String buildDataScopeSql(SysUser user, String deptAlias, String userAlias) {
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, user.getId()));
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

        StringJoiner sqlJoiner = new StringJoiner(" OR ");
        boolean hasFullAccess = false;

        for (SysUserRole ur : userRoles) {
            SysRole role = roleMap.get(ur.getRoleId());
            if (role == null) {
                continue;
            }

            DataScopeEnum scope = DataScopeEnum.of(role.getDataScope());
            switch (scope) {
                case ALL -> hasFullAccess = true;
                case CUSTOM -> appendCustomScope(sqlJoiner, role.getId(), deptAlias);
                case DEPT -> appendDeptScope(sqlJoiner, user.getDeptId(), deptAlias);
                case DEPT_AND_CHILD -> appendDeptAndChildScope(sqlJoiner, user.getDeptId(), deptAlias);
                case SELF -> sqlJoiner.add(userAlias + ".id = " + user.getId());
            }
        }

        if (hasFullAccess) {
            return null;
        }

        String scopeSql = sqlJoiner.toString();
        if (scopeSql.isEmpty()) {
            return null;
        }

        return " AND (" + scopeSql + ")";
    }

    private void appendCustomScope(StringJoiner sqlJoiner, Long roleId, String deptAlias) {
        List<Long> deptIds = sysRoleDeptMapper.selectDeptIdsByRoleId(roleId);
        if (deptIds.isEmpty()) {
            return;
        }
        String inClause = deptIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",", "(", ")"));
        sqlJoiner.add(deptAlias + ".id IN " + inClause);
    }

    private void appendDeptScope(StringJoiner sqlJoiner, Long deptId, String deptAlias) {
        if (deptId != null) {
            sqlJoiner.add(deptAlias + ".id = " + deptId);
        }
    }

    private void appendDeptAndChildScope(StringJoiner sqlJoiner, Long deptId, String deptAlias) {
        if (deptId == null) {
            return;
        }
        SysDept dept = sysDeptMapper.selectById(deptId);
        String ancestors = dept != null ? dept.getAncestors() : null;
        if (ancestors != null && !ANCESTORS_PATTERN.matcher(ancestors).matches()) {
            log.warn("Invalid ancestors format for deptId={}, fallback to dept scope only", deptId);
            ancestors = null;
        }
        if (ancestors != null && !ancestors.isEmpty()) {
            sqlJoiner.add(deptAlias
                    + ".id IN (SELECT id FROM sys_dept WHERE FIND_IN_SET(" + deptId + ", ancestors) > 0"
                    + " OR id = " + deptId + ")");
        } else {
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
    }
}