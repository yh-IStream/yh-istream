package com.istream.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.istream.common.enums.ResultCode;
import com.istream.common.model.dto.SysRoleQuery;
import com.istream.system.entity.SysRole;
import com.istream.system.service.SysMenuService;
import com.istream.system.service.SysRoleService;
import com.istream.system.service.SysUserService;
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
@DisplayName("SysRoleController 接口测试")
class SysRoleControllerTest {

    @Mock
    private SysRoleService sysRoleService;

    @Mock
    private SysMenuService sysMenuService;

    @Mock
    private SysUserService sysUserService;

    @InjectMocks
    private SysRoleController sysRoleController;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(sysRoleController).build();
    }

    private SysRole createRole(Long id, String roleName, String roleKey) {
        SysRole role = new SysRole();
        role.setId(id);
        role.setRoleName(roleName);
        role.setRoleKey(roleKey);
        role.setStatus(0);
        role.setRoleSort(1);
        return role;
    }

    @Nested
    @DisplayName("分页查询")
    class ListTests {

        @Test
        @DisplayName("正常分页查询")
        void list_Success() throws Exception {
            Page<SysRole> page = new Page<>(1, 10);
            page.setRecords(List.of(createRole(1L, "超级管理员", "admin")));
            page.setTotal(1);
            when(sysRoleService.page(any(SysRoleQuery.class))).thenReturn(page);

            mockMvc.perform(get("/system/role/list")
                            .param("pageNum", "1")
                            .param("pageSize", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records[0].roleName").value("超级管理员"));
        }
    }

    @Nested
    @DisplayName("根据ID查询")
    class GetByIdTests {

        @Test
        @DisplayName("角色存在")
        void getById_Success() throws Exception {
            when(sysRoleService.getById(1L)).thenReturn(createRole(1L, "超级管理员", "admin"));

            mockMvc.perform(get("/system/role/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.roleKey").value("admin"));
        }
    }

    @Nested
    @DisplayName("新增角色")
    class AddTests {

        @Test
        @DisplayName("正常新增")
        void add_Success() throws Exception {
            SysRole role = createRole(null, "普通用户", "user");
            when(sysRoleService.count(any(LambdaQueryWrapper.class))).thenReturn(0L);
            when(sysRoleService.save(any(SysRole.class))).thenReturn(true);

            mockMvc.perform(post("/system/role")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(role)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("角色标识已存在")
        void add_DuplicateKey() throws Exception {
            SysRole role = createRole(null, "普通用户", "admin");
            when(sysRoleService.count(any(LambdaQueryWrapper.class))).thenReturn(1L);

            mockMvc.perform(post("/system/role")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(role)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(ResultCode.DATA_DUPLICATE.getCode()));
        }
    }

    @Nested
    @DisplayName("修改角色")
    class UpdateTests {

        @Test
        @DisplayName("正常修改")
        void update_Success() throws Exception {
            SysRole role = createRole(1L, "超级管理员", "admin");
            when(sysRoleService.getOne(any(LambdaQueryWrapper.class))).thenReturn(null);
            when(sysRoleService.updateById(any(SysRole.class))).thenReturn(true);

            mockMvc.perform(put("/system/role")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(role)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("修改后标识与其他角色冲突")
        void update_DuplicateKey() throws Exception {
            SysRole role = createRole(1L, "超级管理员", "user");
            SysRole exist = createRole(2L, "普通用户", "user");
            when(sysRoleService.getOne(any(LambdaQueryWrapper.class))).thenReturn(exist);

            mockMvc.perform(put("/system/role")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(role)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(ResultCode.DATA_DUPLICATE.getCode()));
        }
    }

    @Nested
    @DisplayName("删除角色")
    class DeleteTests {

        @Test
        @DisplayName("角色已分配用户，无法删除")
        void delete_HasUsers() throws Exception {
            when(sysRoleService.hasUsers(1L)).thenReturn(true);

            mockMvc.perform(delete("/system/role/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(ResultCode.HAS_USERS.getCode()));
        }

        @Test
        @DisplayName("正常删除")
        void delete_Success() throws Exception {
            when(sysRoleService.hasUsers(1L)).thenReturn(false);
            when(sysRoleService.removeById(1L)).thenReturn(true);

            mockMvc.perform(delete("/system/role/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("批量删除，有角色已分配用户")
        void deleteBatch_HasUsersAny() throws Exception {
            when(sysRoleService.hasUsersAny(any(List.class))).thenReturn(true);

            mockMvc.perform(delete("/system/role/batch")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(List.of(1L, 2L))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(ResultCode.HAS_USERS.getCode()));
        }

        @Test
        @DisplayName("批量删除，正常")
        void deleteBatch_Success() throws Exception {
            when(sysRoleService.hasUsersAny(any(List.class))).thenReturn(false);
            when(sysRoleService.removeByIds(any(List.class))).thenReturn(true);

            mockMvc.perform(delete("/system/role/batch")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(List.of(1L, 2L))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("修改状态")
    class ChangeStatusTests {

        @Test
        @DisplayName("正常修改")
        void changeStatus_Success() throws Exception {
            when(sysRoleService.changeStatus(1L, 1)).thenReturn(true);
            SysRole request = new SysRole();
            request.setId(1L);
            request.setStatus(1);

            mockMvc.perform(put("/system/role/change-status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }
}