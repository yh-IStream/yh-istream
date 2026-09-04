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
import java.util.StringJoiner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 数据权限 AOP 切面
 * <p>
 * 拦截 @DataScope 注解的方法，根据当前用户角色动态拼接数据权限 SQL 条件，
 * 注入到 {@link BaseQuery#params} 中
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

        String sql = buildDataScopeSql(user, dataScope);
        if (sql != null) {
            injectParams(point.getArgs(), sql);
        }
        return point.proceed();
    }

    private String buildDataScopeSql(SysUser user, DataScope dataScope) {
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
                case ALL -> {
                    hasFullAccess = true;
                }
                case CUSTOM -> {
                    appendCustomScope(sqlJoiner, role.getId(), dataScope);
                }
                case DEPT -> {
                    appendDeptScope(sqlJoiner, user.getDeptId(), dataScope);
                }
                case DEPT_AND_CHILD -> {
                    appendDeptAndChildScope(sqlJoiner, user.getDeptId(), dataScope);
                }
                case SELF -> {
                    sqlJoiner.add(dataScope.userAlias() + ".id = " + user.getId());
                }
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

    private static final Pattern ANCESTORS_PATTERN = Pattern.compile("^[0-9,]*$");

    private void appendCustomScope(StringJoiner sqlJoiner, Long roleId, DataScope dataScope) {
        List<Long> deptIds = sysRoleDeptMapper.selectDeptIdsByRoleId(roleId);
        if (deptIds.isEmpty()) {
            return;
        }
        String inClause = deptIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",", "(", ")"));
        sqlJoiner.add(dataScope.deptAlias() + ".id IN " + inClause);
    }

    private void appendDeptScope(StringJoiner sqlJoiner, Long deptId, DataScope dataScope) {
        if (deptId != null) {
            sqlJoiner.add(dataScope.deptAlias() + ".id = " + deptId);
        }
    }

    private void appendDeptAndChildScope(StringJoiner sqlJoiner, Long deptId, DataScope dataScope) {
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
            sqlJoiner.add(dataScope.deptAlias()
                    + ".id IN (SELECT id FROM sys_dept WHERE FIND_IN_SET(" + deptId + ", ancestors) > 0"
                    + " OR id = " + deptId + ")");
        } else {
            sqlJoiner.add(dataScope.deptAlias() + ".id = " + deptId);
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