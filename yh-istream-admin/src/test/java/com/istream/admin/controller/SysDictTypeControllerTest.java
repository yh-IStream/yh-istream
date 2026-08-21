package com.istream.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.istream.common.enums.ResultCode;
import com.istream.system.entity.SysDictType;
import com.istream.system.service.SysDictTypeService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SysDictTypeController 接口测试")
class SysDictTypeControllerTest {

    @Mock
    private SysDictTypeService sysDictTypeService;

    @InjectMocks
    private SysDictTypeController sysDictTypeController;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(sysDictTypeController).build();
    }

    private SysDictType createDictType(Long id, String dictName, String dictType) {
        SysDictType type = new SysDictType();
        type.setId(id);
        type.setDictName(dictName);
        type.setDictType(dictType);
        type.setStatus(0);
        return type;
    }

    @Nested
    @DisplayName("分页查询")
    class ListTests {

        @Test
        @DisplayName("正常查询，返回字典类型列表")
        void list_Success() throws Exception {
            doAnswer(invocation -> {
                Page<SysDictType> page = invocation.getArgument(0);
                page.setRecords(List.of(
                        createDictType(1L, "用户性别", "sys_user_sex"),
                        createDictType(2L, "菜单类型", "sys_menu_type")));
                page.setTotal(2);
                return null;
            }).when(sysDictTypeService).page(any(Page.class), any(LambdaQueryWrapper.class));

            mockMvc.perform(get("/system/dict-type/list"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records.length()").value(2))
                    .andExpect(jsonPath("$.data.total").value(2))
                    .andExpect(jsonPath("$.data.records[0].dictName").value("用户性别"));
        }
    }

    @Nested
    @DisplayName("新增字典类型")
    class AddTests {

        @Test
        @DisplayName("正常新增")
        void add_Success() throws Exception {
            SysDictType type = createDictType(null, "用户性别", "sys_user_sex");
            when(sysDictTypeService.count(any(LambdaQueryWrapper.class))).thenReturn(0L);
            when(sysDictTypeService.save(any(SysDictType.class))).thenReturn(true);

            mockMvc.perform(post("/system/dict-type")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(type)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("字典类型已存在")
        void add_Duplicate() throws Exception {
            SysDictType type = createDictType(null, "用户性别", "sys_user_sex");
            when(sysDictTypeService.count(any(LambdaQueryWrapper.class))).thenReturn(1L);

            mockMvc.perform(post("/system/dict-type")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(type)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(ResultCode.DATA_DUPLICATE.getCode()));
        }
    }

    @Nested
    @DisplayName("修改字典类型")
    class UpdateTests {

        @Test
        @DisplayName("正常修改")
        void update_Success() throws Exception {
            SysDictType type = createDictType(1L, "用户性别", "sys_user_sex");
            when(sysDictTypeService.getOne(any(LambdaQueryWrapper.class))).thenReturn(null);
            when(sysDictTypeService.updateById(any(SysDictType.class))).thenReturn(true);

            mockMvc.perform(put("/system/dict-type")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(type)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("修改后标识与其他类型冲突")
        void update_Duplicate() throws Exception {
            SysDictType type = createDictType(1L, "用户性别", "sys_menu_type");
            SysDictType exist = createDictType(2L, "菜单类型", "sys_menu_type");
            when(sysDictTypeService.getOne(any(LambdaQueryWrapper.class))).thenReturn(exist);

            mockMvc.perform(put("/system/dict-type")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(type)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(ResultCode.DATA_DUPLICATE.getCode()));
        }
    }

    @Nested
    @DisplayName("删除字典类型")
    class DeleteTests {

        @Test
        @DisplayName("有关联字典数据，无法删除")
        void delete_HasData() throws Exception {
            when(sysDictTypeService.hasDictData(1L)).thenReturn(true);

            mockMvc.perform(delete("/system/dict-type/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(ResultCode.DATA_DUPLICATE.getCode()));
        }

        @Test
        @DisplayName("正常删除")
        void delete_Success() throws Exception {
            when(sysDictTypeService.hasDictData(1L)).thenReturn(false);
            when(sysDictTypeService.removeById(1L)).thenReturn(true);

            mockMvc.perform(delete("/system/dict-type/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("批量删除，有关联数据")
        void deleteBatch_HasData() throws Exception {
            when(sysDictTypeService.hasDictDataAny(any(List.class))).thenReturn(true);

            mockMvc.perform(delete("/system/dict-type/batch")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(List.of(1L, 2L))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(ResultCode.DATA_DUPLICATE.getCode()));
        }

        @Test
        @DisplayName("批量删除，正常")
        void deleteBatch_Success() throws Exception {
            when(sysDictTypeService.hasDictDataAny(any(List.class))).thenReturn(false);
            when(sysDictTypeService.removeByIds(any(List.class))).thenReturn(true);

            mockMvc.perform(delete("/system/dict-type/batch")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(List.of(1L, 2L))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }
}