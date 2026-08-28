package com.istream.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.istream.system.entity.SysMenu;
import com.istream.system.entity.SysRoleMenu;
import com.istream.system.mapper.SysMenuMapper;
import com.istream.system.mapper.SysRoleMenuMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SysMenuServiceImpl 单元测试")
class SysMenuServiceImplTest {

    @Mock
    private SysMenuMapper sysMenuMapper;

    @Mock
    private SysRoleMenuMapper sysRoleMenuMapper;

    @Mock
    private RedissonClient redissonClient;

    private SysMenuServiceImpl sysMenuService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        sysMenuService = new SysMenuServiceImpl(sysRoleMenuMapper, redissonClient);
        ReflectionTestUtils.setField(sysMenuService, "baseMapper", sysMenuMapper);
        RBucket<Object> mockBucket = (RBucket<Object>) org.mockito.Mockito.mock(RBucket.class);
        when(mockBucket.get()).thenReturn(null);
        when(redissonClient.getBucket(anyString())).thenReturn((RBucket) mockBucket);
        Configuration configuration = new Configuration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, SysMenu.class);
        TableInfoHelper.initTableInfo(assistant, SysRoleMenu.class);
    }

    private SysMenu createMenu(Long id, Long parentId, String name, String menuType, Integer orderNum) {
        SysMenu menu = new SysMenu();
        menu.setId(id);
        menu.setParentId(parentId);
        menu.setMenuName(name);
        menu.setMenuType(menuType);
        menu.setOrderNum(orderNum);
        menu.setStatus(0);
        return menu;
    }

    @Test
    @DisplayName("菜单树 — 空列表")
    void listMenuTree_Empty() {
        when(sysMenuMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        List<SysMenu> tree = sysMenuService.listMenuTree();

        assertNotNull(tree);
        assertTrue(tree.isEmpty());
    }

    @Test
    @DisplayName("菜单树 — 单根节点")
    void listMenuTree_SingleRoot() {
        SysMenu root = createMenu(1L, 0L, "系统管理", "M", 1);
        when(sysMenuMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(root));

        List<SysMenu> tree = sysMenuService.listMenuTree();

        assertNotNull(tree);
        assertEquals(1, tree.size());
        assertEquals("系统管理", tree.get(0).getMenuName());
        assertTrue(tree.get(0).getChildren().isEmpty());
    }

    @Test
    @DisplayName("菜单树 — 多级嵌套")
    void listMenuTree_MultiLevel() {
        SysMenu root = createMenu(1L, 0L, "系统管理", "M", 1);
        SysMenu child1 = createMenu(2L, 1L, "用户管理", "C", 1);
        SysMenu child2 = createMenu(3L, 1L, "角色管理", "C", 2);

        when(sysMenuMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(root, child1, child2));

        List<SysMenu> tree = sysMenuService.listMenuTree();

        assertNotNull(tree);
        assertEquals(1, tree.size());
        assertEquals(2, tree.get(0).getChildren().size());
        assertTrue(tree.get(0).getChildren().get(0).getChildren().isEmpty());
    }

    @Test
    @DisplayName("查询全部菜单树 — 包含按钮")
    void listAllMenuTree_WithButtons() {
        SysMenu root = createMenu(1L, 0L, "系统管理", "M", 1);
        SysMenu child = createMenu(2L, 1L, "用户管理", "C", 1);
        SysMenu button = createMenu(3L, 2L, "新增", "F", 1);

        when(sysMenuMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(root, child, button));

        List<SysMenu> tree = sysMenuService.listAllMenuTree();

        assertNotNull(tree);
        assertEquals(1, tree.size());
        assertEquals(1, tree.get(0).getChildren().size());
        assertEquals(1, tree.get(0).getChildren().get(0).getChildren().size());
        assertEquals("新增", tree.get(0).getChildren().get(0).getChildren().get(0).getMenuName());
    }

    @Test
    @DisplayName("存在子菜单")
    void hasChildren_True() {
        when(sysMenuMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(3L);

        boolean result = sysMenuService.hasChildren(1L);

        assertTrue(result);
    }

    @Test
    @DisplayName("不存在子菜单")
    void hasChildren_False() {
        when(sysMenuMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        boolean result = sysMenuService.hasChildren(1L);

        assertFalse(result);
    }

    @Test
    @DisplayName("查询用户权限标识")
    void getPermissionsByUserId() {
        when(sysMenuMapper.selectPermissionsByUserId(1L))
                .thenReturn(List.of("system:user:list", "system:user:add"));

        List<String> permissions = sysMenuService.getPermissionsByUserId(1L);

        assertNotNull(permissions);
        assertEquals(2, permissions.size());
        assertTrue(permissions.contains("system:user:list"));
        assertTrue(permissions.contains("system:user:add"));
    }

    @Test
    @DisplayName("查询用户权限标识 — 无权限")
    void getPermissionsByUserId_Empty() {
        when(sysMenuMapper.selectPermissionsByUserId(1L))
                .thenReturn(Collections.emptyList());

        List<String> permissions = sysMenuService.getPermissionsByUserId(1L);

        assertNotNull(permissions);
        assertTrue(permissions.isEmpty());
    }

    @Test
    @DisplayName("hasRoles — 菜单已分配给角色")
    void hasRoles_True() {
        when(sysRoleMenuMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(2L);

        assertTrue(sysMenuService.hasRoles(1L));
    }

    @Test
    @DisplayName("hasRoles — 菜单未分配给角色")
    void hasRoles_False() {
        when(sysRoleMenuMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        assertFalse(sysMenuService.hasRoles(1L));
    }
}