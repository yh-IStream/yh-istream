package com.istream.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.istream.system.entity.SysDept;
import com.istream.system.entity.SysUser;
import com.istream.system.mapper.SysDeptMapper;
import com.istream.system.mapper.SysUserMapper;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SysDeptServiceImpl 单元测试")
class SysDeptServiceImplTest {

    @Mock
    private SysDeptMapper sysDeptMapper;

    @Mock
    private SysUserMapper sysUserMapper;

    private SysDeptServiceImpl sysDeptService;

    @BeforeEach
    void setUp() {
        sysDeptService = new SysDeptServiceImpl(sysUserMapper);
        ReflectionTestUtils.setField(sysDeptService, "baseMapper", sysDeptMapper);
        Configuration configuration = new Configuration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, SysDept.class);
        TableInfoHelper.initTableInfo(assistant, SysUser.class);
    }

    @Test
    @DisplayName("部门树 — 空列表")
    void listDeptTree_Empty() {
        when(sysDeptMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        List<SysDept> tree = sysDeptService.listDeptTree();

        assertNotNull(tree);
        assertTrue(tree.isEmpty());
    }

    @Test
    @DisplayName("部门树 — 单根节点")
    void listDeptTree_SingleRoot() {
        SysDept root = buildDept(1L, 0L, "总公司");
        when(sysDeptMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(root));

        List<SysDept> tree = sysDeptService.listDeptTree();

        assertEquals(1, tree.size());
        assertEquals("总公司", tree.get(0).getDeptName());
        assertTrue(tree.get(0).getChildren().isEmpty());
    }

    @Test
    @DisplayName("部门树 — 多层嵌套")
    void listDeptTree_Nested() {
        SysDept root = buildDept(1L, 0L, "总公司");
        SysDept child1 = buildDept(2L, 1L, "研发部");
        SysDept child2 = buildDept(3L, 1L, "市场部");
        SysDept grandChild = buildDept(4L, 2L, "后端组");
        when(sysDeptMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(root, child1, child2, grandChild));

        List<SysDept> tree = sysDeptService.listDeptTree();

        assertEquals(1, tree.size());
        SysDept rootNode = tree.get(0);
        assertEquals(2, rootNode.getChildren().size());
        assertEquals(1, rootNode.getChildren().get(0).getChildren().size());
        assertEquals("后端组", rootNode.getChildren().get(0).getChildren().get(0).getDeptName());
    }

    @Test
    @DisplayName("部门树 — parentId 为 0 的根节点")
    void listDeptTree_NullParentId() {
        SysDept root = buildDept(1L, 0L, "总公司");
        when(sysDeptMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(root));

        List<SysDept> tree = sysDeptService.listDeptTree();

        assertEquals(1, tree.size());
    }

    @Test
    @DisplayName("hasChildren — 有子部门")
    void hasChildren_True() {
        when(sysDeptMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(3L);

        assertTrue(sysDeptService.hasChildren(1L));
    }

    @Test
    @DisplayName("hasChildren — 无子部门")
    void hasChildren_False() {
        when(sysDeptMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        assertFalse(sysDeptService.hasChildren(1L));
    }

    @Test
    @DisplayName("hasUsers — 部门下有用户")
    void hasUsers_True() {
        when(sysUserMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

        assertTrue(sysDeptService.hasUsers(1L));
    }

    @Test
    @DisplayName("hasUsers — 部门下无用户")
    void hasUsers_False() {
        when(sysUserMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        assertFalse(sysDeptService.hasUsers(1L));
    }

    private SysDept buildDept(Long id, Long parentId, String name) {
        SysDept dept = new SysDept();
        dept.setId(id);
        dept.setParentId(parentId);
        dept.setDeptName(name);
        dept.setStatus(0);
        dept.setOrderNum(0);
        return dept;
    }
}