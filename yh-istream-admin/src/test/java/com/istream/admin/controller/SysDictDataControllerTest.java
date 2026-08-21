package com.istream.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.istream.system.entity.SysDictData;
import com.istream.system.service.SysDictDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SysDictDataController 接口测试")
class SysDictDataControllerTest {

    @Mock
    private SysDictDataService sysDictDataService;

    @InjectMocks
    private SysDictDataController sysDictDataController;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(sysDictDataController).build();
    }

    private SysDictData createDictData(Long id, String dictType, String label, String value) {
        SysDictData data = new SysDictData();
        data.setId(id);
        data.setDictType(dictType);
        data.setDictLabel(label);
        data.setDictValue(value);
        data.setStatus(0);
        data.setOrderNum(1);
        return data;
    }

    @Nested
    @DisplayName("分页查询")
    class ListTests {

        @Test
        @DisplayName("正常查询")
        void list_Success() throws Exception {
            mockMvc.perform(get("/system/dict-data/list"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").exists());
        }

        @Test
        @DisplayName("按字典类型筛选")
        void list_WithType() throws Exception {
            mockMvc.perform(get("/system/dict-data/list")
                            .param("dictType", "sys_user_sex"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("按类型查询字典数据")
    class GetByTypeTests {

        @Test
        @DisplayName("正常查询")
        void getByType_Success() throws Exception {
            List<SysDictData> list = List.of(
                    createDictData(1L, "sys_user_sex", "男", "0"),
                    createDictData(2L, "sys_user_sex", "女", "1"));
            when(sysDictDataService.list(any(LambdaQueryWrapper.class))).thenReturn(list);

            mockMvc.perform(get("/system/dict-data/by-type/sys_user_sex"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data[0].dictLabel").value("男"))
                    .andExpect(jsonPath("$.data[1].dictLabel").value("女"));
        }

        @Test
        @DisplayName("类型不存在，返回空列表")
        void getByType_Empty() throws Exception {
            when(sysDictDataService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of());

            mockMvc.perform(get("/system/dict-data/by-type/nonexistent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    @DisplayName("获取字典Map")
    class DictMapTests {

        @Test
        @DisplayName("正常获取")
        void dictMap_Success() throws Exception {
            Map<String, List<SysDictData>> map = Map.of(
                    "sys_user_sex", List.of(createDictData(1L, "sys_user_sex", "男", "0")));
            when(sysDictDataService.getDictMap()).thenReturn(map);

            mockMvc.perform(get("/system/dict-data/map"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.sys_user_sex").exists());
        }
    }

    @Nested
    @DisplayName("根据ID查询")
    class GetByIdTests {

        @Test
        @DisplayName("正常查询")
        void getById_Success() throws Exception {
            SysDictData data = createDictData(1L, "sys_user_sex", "男", "0");
            when(sysDictDataService.getById(1L)).thenReturn(data);

            mockMvc.perform(get("/system/dict-data/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.dictLabel").value("男"));
        }

        @Test
        @DisplayName("ID不存在")
        void getById_NotFound() throws Exception {
            when(sysDictDataService.getById(999L)).thenReturn(null);

            mockMvc.perform(get("/system/dict-data/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    @DisplayName("新增字典数据")
    class AddTests {

        @Test
        @DisplayName("正常新增")
        void add_Success() throws Exception {
            SysDictData data = createDictData(null, "sys_user_sex", "保密", "2");
            when(sysDictDataService.save(any(SysDictData.class))).thenReturn(true);

            mockMvc.perform(post("/system/dict-data")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(data)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("修改字典数据")
    class UpdateTests {

        @Test
        @DisplayName("正常修改")
        void update_Success() throws Exception {
            SysDictData data = createDictData(1L, "sys_user_sex", "男", "0");
            when(sysDictDataService.updateById(any(SysDictData.class))).thenReturn(true);

            mockMvc.perform(put("/system/dict-data")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(data)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("删除字典数据")
    class DeleteTests {

        @Test
        @DisplayName("正常删除")
        void delete_Success() throws Exception {
            when(sysDictDataService.removeById(1L)).thenReturn(true);

            mockMvc.perform(delete("/system/dict-data/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("批量删除")
        void deleteBatch_Success() throws Exception {
            when(sysDictDataService.removeByIds(any(List.class))).thenReturn(true);

            mockMvc.perform(delete("/system/dict-data/batch")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(List.of(1L, 2L, 3L))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }
}