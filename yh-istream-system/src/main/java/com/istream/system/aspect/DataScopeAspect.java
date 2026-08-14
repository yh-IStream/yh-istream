package com.istream.system.aspect;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.istream.common.annotation.DataScope;
import com.istream.common.enums.DataScopeEnum;
import com.istream.common.model.BaseQuery;
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
import java.util.Objects;
import java.util.StringJoiner;
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
        try {
            Long userId = SecurityUtils.getLoginUserId();
            SysUser user = sysUserMapper.selectById(userId);
            if (user == null) {
                return point.proceed();
            }

            String sql = buildDataScopeSql(user, dataScope);
            if (sql != null) {
                injectParams(point.getArgs(), sql);
            }
        } catch (Exception e) {
            log.debug("数据权限处理异常（非登录态时忽略）: {}", e.getMessage());
        }
        return point.proceed();
    }

    private String buildDataScopeSql(SysUser user, DataScope dataScope) {
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, user.getId()));
        if (userRoles.isEmpty()) {
            return null;
        }

        StringJoiner sqlJoiner = new StringJoiner(" OR ");
        boolean hasFullAccess = false;

        for (SysUserRole ur : userRoles) {
            SysRole role = sysRoleMapper.selectById(ur.getRoleId());
            if (role == null || Objects.equals(role.getStatus(), 1)) {
                continue;
            }

            DataScopeEnum scope = DataScopeEnum.of(role.getDataScope());
            switch (scope) {
                case ALL -> {
                    hasFullAccess = true;
                }
                case CUSTOM -> {
                    List<Long> deptIds = sysRoleDeptMapper.selectDeptIdsByRoleId(role.getId());
                    if (!deptIds.isEmpty()) {
                        String inClause = deptIds.stream()
                                .map(String::valueOf)
                                .collect(Collectors.joining(",", "(", ")"));
                        sqlJoiner.add(dataScope.deptAlias() + ".id IN " + inClause);
                    }
                }
                case DEPT -> {
                    if (user.getDeptId() != null) {
                        sqlJoiner.add(dataScope.deptAlias() + ".id = " + user.getDeptId());
                    }
                }
                case DEPT_AND_CHILD -> {
                    if (user.getDeptId() != null) {
                        SysDept dept = sysDeptMapper.selectById(user.getDeptId());
                        String ancestorFilter = dept != null
                                ? dept.getAncestors() + "," + user.getDeptId()
                                : String.valueOf(user.getDeptId());
                        sqlJoiner.add(dataScope.deptAlias() + ".id IN (SELECT id FROM sys_dept WHERE ancestors LIKE '%" + ancestorFilter + "%' OR id = " + user.getDeptId() + ")");
                    }
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

    private void injectParams(Object[] args, String sql) {
        for (Object arg : args) {
            if (arg instanceof BaseQuery query) {
                query.getParams().put(DataScope.DATA_SCOPE_KEY, sql);
                return;
            }
        }
    }
}