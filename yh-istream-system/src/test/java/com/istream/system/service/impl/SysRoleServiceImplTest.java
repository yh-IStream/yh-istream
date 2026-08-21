package com.istream.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.istream.common.model.dto.SysRoleQuery;
import com.istream.system.entity.SysRole;
import com.istream.system.entity.SysRoleMenu;
import com.istream.system.entity.SysUserRole;
import com.istream.system.mapper.SysRoleMapper;
import com.istream.system.mapper.SysRoleMenuMapper;
import com.istream.system.mapper.SysUserRoleMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SysRoleServiceImpl 单元测试")
class SysRoleServiceImplTest {

    @Mock
    private SysRoleMapper sysRoleMapper;

    @Mock
    private SysRoleMenuMapper sysRoleMenuMapper;

    @Mock
    private SysUserRoleMapper sysUserRoleMapper;

    private SysRoleServiceImpl sysRoleService;

    @BeforeEach
    void setUp() {
        sysRoleService = new SysRoleServiceImpl(sysRoleMenuMapper, sysUserRoleMapper);
        ReflectionTestUtils.setField(sysRoleService, "baseMapper", sysRoleMapper);
        Configuration configuration = new Configuration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, SysRole.class);
        TableInfoHelper.initTableInfo(assistant, SysRoleMenu.class);
        TableInfoHelper.initTableInfo(assistant, SysUserRole.class);
    }

    @Test
    @DisplayName("查询角色关联菜单 — 返回菜单ID列表")
    void getMenuIdsByRoleId_HasMenus() {
        SysRoleMenu rm1 = new SysRoleMenu();
        rm1.setRoleId(1L);
        rm1.setMenuId(10L);
        SysRoleMenu rm2 = new SysRoleMenu();
        rm2.setRoleId(1L);
        rm2.setMenuId(20L);

        when(sysRoleMenuMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(rm1, rm2));

        List<Long> result = sysRoleService.getMenuIdsByRoleId(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(10L));
        assertTrue(result.contains(20L));
    }

    @Test
    @DisplayName("查询角色关联菜单 — 无关联菜单")
    void getMenuIdsByRoleId_Empty() {
        when(sysRoleMenuMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        List<Long> result = sysRoleService.getMenuIdsByRoleId(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("保存角色菜单 — 正常分配")
    void saveRoleMenu_WithMenuIds() {
        List<Long> menuIds = List.of(10L, 20L, 30L);

        sysRoleService.saveRoleMenu(1L, menuIds);

        verify(sysRoleMenuMapper).delete(any(LambdaQueryWrapper.class));
        ArgumentCaptor<SysRoleMenu> captor = ArgumentCaptor.forClass(SysRoleMenu.class);
        verify(sysRoleMenuMapper, times(3)).insert(captor.capture());

        List<SysRoleMenu> inserted = captor.getAllValues();
        assertEquals(1L, inserted.get(0).getRoleId());
        assertEquals(10L, inserted.get(0).getMenuId());
        assertEquals(1L, inserted.get(1).getRoleId());
        assertEquals(20L, inserted.get(1).getMenuId());
        assertEquals(1L, inserted.get(2).getRoleId());
        assertEquals(30L, inserted.get(2).getMenuId());
    }

    @Test
    @DisplayName("保存角色菜单 — 清空菜单（menuIds=null）")
    void saveRoleMenu_NullMenuIds() {
        sysRoleService.saveRoleMenu(1L, null);

        verify(sysRoleMenuMapper).delete(any(LambdaQueryWrapper.class));
        verify(sysRoleMenuMapper, never()).insert(any(SysRoleMenu.class));
    }

    @Test
    @DisplayName("保存角色菜单 — 清空菜单（menuIds=空列表）")
    void saveRoleMenu_EmptyMenuIds() {
        sysRoleService.saveRoleMenu(1L, Collections.emptyList());

        verify(sysRoleMenuMapper).delete(any(LambdaQueryWrapper.class));
        verify(sysRoleMenuMapper, never()).insert(any(SysRoleMenu.class));
    }

    @Test
    @DisplayName("变更角色状态")
    void changeStatus() {
        when(sysRoleMapper.updateById(any(SysRole.class))).thenReturn(1);

        boolean result = sysRoleService.changeStatus(1L, 0);

        assertTrue(result);
        ArgumentCaptor<SysRole> captor = ArgumentCaptor.forClass(SysRole.class);
        verify(sysRoleMapper).updateById(captor.capture());
        assertEquals(1L, captor.getValue().getId());
        assertEquals(0, captor.getValue().getStatus());
    }

    @Test
    @DisplayName("hasUsers — 角色已分配用户")
    void hasUsers_True() {
        when(sysUserRoleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(3L);

        assertTrue(sysRoleService.hasUsers(1L));
    }

    @Test
    @DisplayName("hasUsers — 角色未分配用户")
    void hasUsers_False() {
        when(sysUserRoleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        assertFalse(sysRoleService.hasUsers(1L));
    }

    @Test
    @DisplayName("hasUsersAny — 批量角色中有已分配用户的")
    void hasUsersAny_True() {
        when(sysUserRoleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(2L);

        assertTrue(sysRoleService.hasUsersAny(List.of(1L, 2L, 3L)));
    }

    @Test
    @DisplayName("hasUsersAny — 批量角色均未分配用户")
    void hasUsersAny_False() {
        when(sysUserRoleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        assertFalse(sysRoleService.hasUsersAny(List.of(1L, 2L)));
    }

    @Test
    @DisplayName("hasUsersAny — 空列表")
    void hasUsersAny_Empty() {
        assertFalse(sysRoleService.hasUsersAny(Collections.emptyList()));
    }

    @Test
    @DisplayName("hasUsersAny — null 列表")
    void hasUsersAny_Null() {
        assertFalse(sysRoleService.hasUsersAny(null));
    }
}