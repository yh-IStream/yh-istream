package com.istream.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.istream.system.entity.SysDictData;
import com.istream.system.entity.SysDictType;
import com.istream.system.mapper.SysDictDataMapper;
import com.istream.system.mapper.SysDictTypeMapper;
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

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SysDictTypeServiceImpl 单元测试")
class SysDictTypeServiceImplTest {

    @Mock
    private SysDictTypeMapper sysDictTypeMapper;

    @Mock
    private SysDictDataMapper sysDictDataMapper;

    private SysDictTypeServiceImpl sysDictTypeService;

    @BeforeEach
    void setUp() {
        sysDictTypeService = new SysDictTypeServiceImpl(sysDictDataMapper);
        ReflectionTestUtils.setField(sysDictTypeService, "baseMapper", sysDictTypeMapper);
        Configuration configuration = new Configuration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, SysDictType.class);
        TableInfoHelper.initTableInfo(assistant, SysDictData.class);
    }

    private SysDictType createDictType(Long id, String dictName, String dictType) {
        SysDictType type = new SysDictType();
        type.setId(id);
        type.setDictName(dictName);
        type.setDictType(dictType);
        type.setStatus(0);
        return type;
    }

    @Test
    @DisplayName("hasDictData — 字典类型存在关联数据")
    void hasDictData_True() {
        SysDictType dictType = createDictType(1L, "用户性别", "sys_user_sex");
        when(sysDictTypeMapper.selectById(1L)).thenReturn(dictType);
        when(sysDictDataMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

        assertTrue(sysDictTypeService.hasDictData(1L));
    }

    @Test
    @DisplayName("hasDictData — 字典类型无关联数据")
    void hasDictData_False() {
        SysDictType dictType = createDictType(1L, "用户性别", "sys_user_sex");
        when(sysDictTypeMapper.selectById(1L)).thenReturn(dictType);
        when(sysDictDataMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        assertFalse(sysDictTypeService.hasDictData(1L));
    }

    @Test
    @DisplayName("hasDictData — 字典类型不存在")
    void hasDictData_TypeNotFound() {
        when(sysDictTypeMapper.selectById(99L)).thenReturn(null);

        assertFalse(sysDictTypeService.hasDictData(99L));
    }

    @Test
    @DisplayName("hasDictDataAny — 批量字典类型中有关联数据")
    void hasDictDataAny_True() {
        List<SysDictType> dictTypes = List.of(
                createDictType(1L, "用户性别", "sys_user_sex"),
                createDictType(2L, "菜单类型", "sys_menu_type")
        );
        when(sysDictTypeMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(dictTypes);
        when(sysDictDataMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(3L);

        assertTrue(sysDictTypeService.hasDictDataAny(List.of(1L, 2L)));
    }

    @Test
    @DisplayName("hasDictDataAny — 批量字典类型中无关联数据")
    void hasDictDataAny_False() {
        List<SysDictType> dictTypes = List.of(
                createDictType(1L, "用户性别", "sys_user_sex"),
                createDictType(2L, "菜单类型", "sys_menu_type")
        );
        when(sysDictTypeMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(dictTypes);
        when(sysDictDataMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        assertFalse(sysDictTypeService.hasDictDataAny(List.of(1L, 2L)));
    }

    @Test
    @DisplayName("hasDictDataAny — 空列表")
    void hasDictDataAny_EmptyList() {
        assertFalse(sysDictTypeService.hasDictDataAny(Collections.emptyList()));
    }

    @Test
    @DisplayName("hasDictDataAny — null 列表")
    void hasDictDataAny_NullList() {
        assertFalse(sysDictTypeService.hasDictDataAny(null));
    }

    @Test
    @DisplayName("hasDictDataAny — 所有字典类型均不存在")
    void hasDictDataAny_AllTypesNotFound() {
        when(sysDictTypeMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        assertFalse(sysDictTypeService.hasDictDataAny(List.of(1L, 2L)));
    }
}