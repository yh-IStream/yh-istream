package com.istream.system.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.istream.common.exception.BusinessException;
import com.istream.system.entity.SysUser;
import com.istream.system.entity.SysUserRole;
import com.istream.system.mapper.SysRoleMapper;
import com.istream.system.mapper.SysUserMapper;
import com.istream.system.mapper.SysUserRoleMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SysUserServiceImpl 单元测试")
class SysUserServiceImplTest {

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private SysUserRoleMapper sysUserRoleMapper;

    @Mock
    private SysRoleMapper sysRoleMapper;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RBucket<Object> rBucket;

    private SysUserServiceImpl sysUserService;

    @BeforeEach
    void setUp() {
        sysUserService = new SysUserServiceImpl(sysUserRoleMapper, sysRoleMapper, redissonClient);
        ReflectionTestUtils.setField(sysUserService, "baseMapper", sysUserMapper);
        when(redissonClient.getBucket(anyString())).thenReturn(rBucket);
        when(rBucket.delete()).thenReturn(true);
        Configuration configuration = new Configuration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, SysUser.class);
        TableInfoHelper.initTableInfo(assistant, SysUserRole.class);
    }

    @Test
    @DisplayName("创建用户 — 成功")
    void createUser_Success() {
        try (MockedStatic<BCrypt> bcryptMock = mockStatic(BCrypt.class)) {
            bcryptMock.when(() -> BCrypt.hashpw(anyString())).thenReturn("$2a$10$encrypted_hash");
            when(sysUserMapper.selectOne(any(), anyBoolean())).thenReturn(null);
            when(sysUserMapper.insert(any(SysUser.class))).thenReturn(1);

            SysUser user = new SysUser();
            user.setUsername("testuser");
            user.setPassword("123456");

            assertDoesNotThrow(() -> sysUserService.createUser(user));
            verify(sysUserMapper).insert(any(SysUser.class));
        }
    }

    @Test
    @DisplayName("创建用户 — 用户名重复")
    void createUser_DuplicateUsername() {
        SysUser existUser = new SysUser();
        existUser.setUsername("testuser");
        when(sysUserMapper.selectOne(any(), anyBoolean())).thenReturn(existUser);

        SysUser user = new SysUser();
        user.setUsername("testuser");
        user.setPassword("123456");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> sysUserService.createUser(user));
        assertTrue(ex.getMessage().contains("用户名已存在"));
    }

    @Test
    @DisplayName("更新用户 — 成功")
    void updateUser_Success() {
        when(sysUserMapper.selectOne(any(), anyBoolean())).thenReturn(null);
        when(sysUserMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);

        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("updatedUser");

        assertDoesNotThrow(() -> sysUserService.updateUser(user));
        verify(sysUserMapper).update(isNull(), any(LambdaUpdateWrapper.class));
    }

    @Test
    @DisplayName("更新用户 — 用户名被其他用户占用")
    void updateUser_DuplicateUsername() {
        SysUser existUser = new SysUser();
        existUser.setId(2L);
        existUser.setUsername("duplicate");
        when(sysUserMapper.selectOne(any(), anyBoolean())).thenReturn(existUser);

        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("duplicate");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> sysUserService.updateUser(user));
        assertTrue(ex.getMessage().contains("用户名已存在"));
    }

    @Test
    @DisplayName("更新用户 — 用户名不变（自身）")
    void updateUser_SameUser() {
        SysUser existUser = new SysUser();
        existUser.setId(1L);
        existUser.setUsername("same");
        when(sysUserMapper.selectOne(any(), anyBoolean())).thenReturn(existUser);
        when(sysUserMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);

        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("same");

        assertDoesNotThrow(() -> sysUserService.updateUser(user));
    }

    @Test
    @DisplayName("重置密码 — 成功")
    void resetPassword_Success() {
        try (MockedStatic<BCrypt> bcryptMock = mockStatic(BCrypt.class)) {
            bcryptMock.when(() -> BCrypt.hashpw(anyString())).thenReturn("$2a$10$encrypted_hash");
            when(sysUserMapper.updateById(any(SysUser.class))).thenReturn(1);

            boolean result = sysUserService.resetPassword(1L, "newPassword");

            assertTrue(result);
            verify(sysUserMapper).updateById(any(SysUser.class));
        }
    }

    @Test
    @DisplayName("修改状态 — 成功")
    void changeStatus_Success() {
        when(sysUserMapper.updateById(any(SysUser.class))).thenReturn(1);

        boolean result = sysUserService.changeStatus(1L, 1);

        assertTrue(result);
    }

    @Test
    @DisplayName("根据角色ID查询用户 — 有用户")
    void getUsersByRoleId_WithUsers() {
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(1L);
        userRole.setRoleId(1L);
        when(sysUserRoleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(userRole));
        when(sysUserMapper.selectByIds(any())).thenReturn(List.of(new SysUser()));

        List<SysUser> users = sysUserService.getUsersByRoleId(1L);

        assertFalse(users.isEmpty());
        assertEquals(1, users.size());
    }

    @Test
    @DisplayName("根据角色ID查询用户 — 无用户")
    void getUsersByRoleId_Empty() {
        when(sysUserRoleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        List<SysUser> users = sysUserService.getUsersByRoleId(1L);

        assertTrue(users.isEmpty());
    }

    @Test
    @DisplayName("更新登录信息 — 成功")
    void updateLoginInfo_Success() {
        when(sysUserMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);

        assertDoesNotThrow(() -> sysUserService.updateLoginInfo(1L, "127.0.0.1"));
        verify(sysUserMapper).update(isNull(), any(LambdaUpdateWrapper.class));
    }
}