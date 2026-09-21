package com.istream.system.aspect;

import com.istream.common.annotation.DataScope;
import com.istream.common.constant.Constants;
import com.istream.common.enums.DataScopeEnum;
import com.istream.common.enums.StatusEnum;
import com.istream.common.model.query.BaseQuery;
import com.istream.framework.cache.CacheService;
import com.istream.system.entity.SysDept;
import com.istream.system.entity.SysRole;
import com.istream.system.entity.SysUser;
import com.istream.system.entity.SysUserRole;
import com.istream.system.mapper.SysDeptMapper;
import com.istream.system.mapper.SysRoleDeptMapper;
import com.istream.system.mapper.SysRoleMapper;
import com.istream.system.mapper.SysUserMapper;
import com.istream.system.mapper.SysUserRoleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * DataScopeAspect 单元测试
 *
 * <p>验证数据权限切面的核心行为：别名校验、SQL 拼接、参数注入。</p>
 *
 * @author istream
 * @since 2026-09-19
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DataScopeAspect 数据权限切面")
class DataScopeAspectTest {

    @Mock
    private SysUserMapper sysUserMapper;
    @Mock
    private SysRoleMapper sysRoleMapper;
    @Mock
    private SysUserRoleMapper sysUserRoleMapper;
    @Mock
    private SysRoleDeptMapper sysRoleDeptMapper;
    @Mock
    private SysDeptMapper sysDeptMapper;
    @Mock
    private CacheService cacheService;

    @InjectMocks
    private DataScopeAspect dataScopeAspect;

    @Test
    @DisplayName("白名单别名 d 合法")
    void shouldAcceptWhitelistedAliasD() {
        String result = ReflectionTestUtils.invokeMethod(dataScopeAspect, "validateAlias", "d", "deptAlias");
        assertEquals("d", result);
    }

    @Test
    @DisplayName("白名单别名 u 合法")
    void shouldAcceptWhitelistedAliasU() {
        String result = ReflectionTestUtils.invokeMethod(dataScopeAspect, "validateAlias", "u", "userAlias");
        assertEquals("u", result);
    }

    @Test
    @DisplayName("白名单别名 dept 合法")
    void shouldAcceptWhitelistedAliasDept() {
        String result = ReflectionTestUtils.invokeMethod(dataScopeAspect, "validateAlias", "dept", "deptAlias");
        assertEquals("dept", result);
    }

    @Test
    @DisplayName("符合正则的非白名单别名合法")
    void shouldAcceptNonWhitelistedAliasMatchingPattern() {
        String result = ReflectionTestUtils.invokeMethod(dataScopeAspect, "validateAlias", "t1", "deptAlias");
        assertEquals("t1", result);
    }

    @Test
    @DisplayName("空别名抛异常")
    void shouldRejectEmptyAlias() {
        assertThrows(IllegalArgumentException.class,
                () -> ReflectionTestUtils.invokeMethod(dataScopeAspect, "validateAlias", "", "deptAlias"));
    }

    @Test
    @DisplayName("null 别名抛异常")
    void shouldRejectNullAlias() {
        assertThrows(IllegalArgumentException.class,
                () -> ReflectionTestUtils.invokeMethod(dataScopeAspect, "validateAlias", null, "deptAlias"));
    }

    @Test
    @DisplayName("包含特殊字符的别名抛异常")
    void shouldRejectAliasWithSpecialChars() {
        assertThrows(IllegalArgumentException.class,
                () -> ReflectionTestUtils.invokeMethod(dataScopeAspect, "validateAlias", "d;--", "deptAlias"));
    }

    @Test
    @DisplayName("超长别名抛异常（超过31字符）")
    void shouldRejectOverlyLongAlias() {
        String longAlias = "a".repeat(32);
        assertThrows(IllegalArgumentException.class,
                () -> ReflectionTestUtils.invokeMethod(dataScopeAspect, "validateAlias", longAlias, "deptAlias"));
    }

    @Test
    @DisplayName("injectParams 将 SQL 注入 BaseQuery.params")
    void shouldInjectSqlIntoBaseQueryParams() {
        BaseQuery query = new BaseQuery();
        Object[] args = new Object[]{query};
        String sql = " AND (d.id = 1)";

        ReflectionTestUtils.invokeMethod(dataScopeAspect, "injectParams", args, sql);

        assertEquals(sql, query.getParams().get(DataScope.DATA_SCOPE_KEY));
    }

    @Test
    @DisplayName("injectParams 无 BaseQuery 参数时不报错")
    void shouldNotFailWhenNoBaseQueryInArgs() {
        Object[] args = new Object[]{"stringArg", 123};
        ReflectionTestUtils.invokeMethod(dataScopeAspect, "injectParams", args, " AND (d.id = 1)");
    }

    @Test
    @DisplayName("ALL 角色不加任何限制（返回 null）")
    void shouldReturnNullForAllScopeRole() {
        Long userId = 1L;
        setupUserWithRole(userId, DataScopeEnum.ALL.getCode());

        String sql = ReflectionTestUtils.invokeMethod(dataScopeAspect,
                "buildDataScopeSql", userId, "d", "u");

        assertNull(sql);
    }

    @Test
    @DisplayName("SELF 角色拼接 user.id = userId")
    void shouldBuildSelfScopeSql() {
        Long userId = 1L;
        setupUserWithRole(userId, DataScopeEnum.SELF.getCode());

        String sql = ReflectionTestUtils.invokeMethod(dataScopeAspect,
                "buildDataScopeSql", userId, "d", "u");

        assertNotNull(sql);
        assertEquals(" AND (u.id = 1)", sql);
    }

    @Test
    @DisplayName("DEPT 角色拼接 dept.id = deptId")
    void shouldBuildDeptScopeSql() {
        Long userId = 1L;
        Long deptId = 10L;
        setupUserWithDeptRole(userId, deptId, DataScopeEnum.DEPT.getCode());

        String sql = ReflectionTestUtils.invokeMethod(dataScopeAspect,
                "buildDataScopeSql", userId, "d", "u");

        assertNotNull(sql);
        assertEquals(" AND (d.id = 10)", sql);
    }

    private void setupUserWithRole(Long userId, Integer dataScope) {
        SysUser user = new SysUser();
        user.setId(userId);
        user.setDeptId(10L);
        when(sysUserMapper.selectById(userId)).thenReturn(user);

        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(100L);
        when(sysUserRoleMapper.selectList(any())).thenReturn(List.of(userRole));

        SysRole role = new SysRole();
        role.setId(100L);
        role.setDataScope(dataScope);
        role.setStatus(StatusEnum.ENABLED.getCode());
        when(sysRoleMapper.selectByIds(List.of(100L))).thenReturn(List.of(role));

        when(cacheService.get(any())).thenReturn(null);
    }

    private void setupUserWithDeptRole(Long userId, Long deptId, Integer dataScope) {
        SysUser user = new SysUser();
        user.setId(userId);
        user.setDeptId(deptId);
        when(sysUserMapper.selectById(userId)).thenReturn(user);

        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(100L);
        when(sysUserRoleMapper.selectList(any())).thenReturn(List.of(userRole));

        SysRole role = new SysRole();
        role.setId(100L);
        role.setDataScope(dataScope);
        role.setStatus(StatusEnum.ENABLED.getCode());
        when(sysRoleMapper.selectByIds(List.of(100L))).thenReturn(List.of(role));

        when(cacheService.get(any())).thenReturn(null);
    }
}