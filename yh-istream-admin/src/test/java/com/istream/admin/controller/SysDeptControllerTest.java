package com.istream.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.istream.common.enums.ResultCode;
import com.istream.system.entity.SysDept;
import com.istream.system.service.SysDeptService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SysDeptController 接口测试")
class SysDeptControllerTest {

    @Mock
    private SysDeptService sysDeptService;

    @InjectMocks
    private SysDeptController sysDeptController;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(sysDeptController).build();
    }

    private SysDept createDept(Long id, String deptName, Long parentId) {
        SysDept dept = new SysDept();
        dept.setId(id);
        dept.setDeptName(deptName);
        dept.setParentId(parentId);
        dept.setStatus(0);
        return dept;
    }

    @Nested
    @DisplayName("部门树查询")
    class TreeTests {

        @Test
        @DisplayName("正常查询")
        void tree_Success() throws Exception {
            when(sysDeptService.listDeptTree()).thenReturn(List.of(
                    createDept(1L, "总公司", 0L),
                    createDept(2L, "研发部", 1L)
            ));

            mockMvc.perform(get("/system/dept/tree"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data[0].deptName").value("总公司"));
        }
    }

    @Nested
    @DisplayName("根据ID查询")
    class GetByIdTests {

        @Test
        @DisplayName("部门存在")
        void getById_Success() throws Exception {
            when(sysDeptService.getById(1L)).thenReturn(createDept(1L, "总公司", 0L));

            mockMvc.perform(get("/system/dept/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.deptName").value("总公司"));
        }
    }

    @Nested
    @DisplayName("新增部门")
    class AddTests {

        @Test
        @DisplayName("正常新增")
        void add_Success() throws Exception {
            SysDept dept = createDept(null, "研发部", 1L);
            when(sysDeptService.save(any(SysDept.class))).thenReturn(true);

            mockMvc.perform(post("/system/dept")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dept)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("修改部门")
    class UpdateTests {

        @Test
        @DisplayName("正常修改")
        void update_Success() throws Exception {
            SysDept dept = createDept(2L, "研发部", 1L);
            when(sysDeptService.updateById(any(SysDept.class))).thenReturn(true);

            mockMvc.perform(put("/system/dept")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dept)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("上级部门不能是自己")
        void update_ParentIsSelf() throws Exception {
            SysDept dept = createDept(1L, "总公司", 1L);

            mockMvc.perform(put("/system/dept")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dept)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(ResultCode.PARAM_VALID_ERROR.getCode()));
        }
    }

    @Nested
    @DisplayName("删除部门")
    class DeleteTests {

        @Test
        @DisplayName("有子部门，无法删除")
        void delete_HasChildren() throws Exception {
            when(sysDeptService.hasChildren(1L)).thenReturn(true);

            mockMvc.perform(delete("/system/dept/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(ResultCode.HAS_CHILDREN.getCode()));
        }

        @Test
        @DisplayName("有用户关联，无法删除")
        void delete_HasUsers() throws Exception {
            when(sysDeptService.hasChildren(1L)).thenReturn(false);
            when(sysDeptService.hasUsers(1L)).thenReturn(true);

            mockMvc.perform(delete("/system/dept/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(ResultCode.HAS_USERS.getCode()));
        }

        @Test
        @DisplayName("正常删除")
        void delete_Success() throws Exception {
            when(sysDeptService.hasChildren(1L)).thenReturn(false);
            when(sysDeptService.hasUsers(1L)).thenReturn(false);
            when(sysDeptService.removeById(1L)).thenReturn(true);

            mockMvc.perform(delete("/system/dept/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }
}