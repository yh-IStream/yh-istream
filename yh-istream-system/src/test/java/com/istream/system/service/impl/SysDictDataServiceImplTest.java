package com.istream.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.istream.system.entity.SysDictData;
import com.istream.system.mapper.SysDictDataMapper;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SysDictDataServiceImpl 单元测试")
class SysDictDataServiceImplTest {

    @Mock
    private SysDictDataMapper sysDictDataMapper;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RBucket<Map<String, List<SysDictData>>> bucket;

    private SysDictDataServiceImpl sysDictDataService;

    @BeforeEach
    void setUp() {
        when(redissonClient.<Map<String, List<SysDictData>>>getBucket(anyString())).thenReturn(bucket);
        when(bucket.get()).thenReturn(null);

        sysDictDataService = new SysDictDataServiceImpl(redissonClient);
        ReflectionTestUtils.setField(sysDictDataService, "baseMapper", sysDictDataMapper);
        Configuration configuration = new Configuration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, SysDictData.class);
    }

    private SysDictData createDictData(Long id, String dictType, String label, String value, Integer orderNum) {
        SysDictData data = new SysDictData();
        data.setId(id);
        data.setDictType(dictType);
        data.setDictLabel(label);
        data.setDictValue(value);
        data.setOrderNum(orderNum);
        data.setStatus(0);
        return data;
    }

    @Test
    @DisplayName("字典Map — 空列表")
    void getDictMap_Empty() {
        when(sysDictDataMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        Map<String, List<SysDictData>> result = sysDictDataService.getDictMap();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("字典Map — 单一类型")
    void getDictMap_SingleType() {
        List<SysDictData> data = List.of(
                createDictData(1L, "sys_user_sex", "男", "0", 1),
                createDictData(2L, "sys_user_sex", "女", "1", 2)
        );
        when(sysDictDataMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(data);

        Map<String, List<SysDictData>> result = sysDictDataService.getDictMap();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("sys_user_sex"));
        assertEquals(2, result.get("sys_user_sex").size());
    }

    @Test
    @DisplayName("字典Map — 多种类型")
    void getDictMap_MultipleTypes() {
        List<SysDictData> data = List.of(
                createDictData(1L, "sys_user_sex", "男", "0", 1),
                createDictData(2L, "sys_user_sex", "女", "1", 2),
                createDictData(3L, "sys_show_hide", "显示", "0", 1),
                createDictData(4L, "sys_show_hide", "隐藏", "1", 2)
        );
        when(sysDictDataMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(data);

        Map<String, List<SysDictData>> result = sysDictDataService.getDictMap();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("sys_user_sex"));
        assertTrue(result.containsKey("sys_show_hide"));
        assertEquals(2, result.get("sys_user_sex").size());
        assertEquals(2, result.get("sys_show_hide").size());
    }
}