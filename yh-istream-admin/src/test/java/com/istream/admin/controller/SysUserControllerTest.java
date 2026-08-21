package com.istream.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.istream.common.model.R;
import com.istream.common.model.dto.SysUserQuery;
import com.istream.common.enums.ResultCode;
import com.istream.system.entity.SysUser;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SysUserController 接口测试")
class SysUserControllerTest {

    @Mock
    private SysUserService sysUserService;

    @InjectMocks
    private SysUserController sysUserController;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(sysUserController).build();
    }

    private SysUser createUser(Long id, String username, String nickname) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setUsername(username);
        user.setNickname(nickname);
        user.setStatus(0);
        return user;
    }

    @Nested
    @DisplayName("分页查询")
    class ListTests {

        @Test
        @DisplayName("正常分页查询")
        void list_Success() throws Exception {
            Page<SysUser> page = new Page<>(1, 10);
            page.setRecords(List.of(createUser(1L, "admin", "管理员")));
            page.setTotal(1);
            when(sysUserService.page(any(SysUserQuery.class))).thenReturn(page);

            mockMvc.perform(get("/system/user/list")
                            .param("pageNum", "1")
                            .param("pageSize", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records[0].username").value("admin"));
        }
    }

    @Nested
    @DisplayName("根据ID查询")
    class GetByIdTests {

        @Test
        @DisplayName("用户存在")
        void getById_Success() throws Exception {
            SysUser user = createUser(1L, "admin", "管理员");
            when(sysUserService.getById(1L)).thenReturn(user);

            mockMvc.perform(get("/system/user/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.username").value("admin"));
        }

        @Test
        @DisplayName("用户不存在")
        void getById_NotFound() throws Exception {
            when(sysUserService.getById(99L)).thenReturn(null);

            mockMvc.perform(get("/system/user/99"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(ResultCode.USER_NOT_EXIST.getCode()));
        }
    }

    @Nested
    @DisplayName("新增用户")
    class AddTests {

        @Test
        @DisplayName("正常新增")
        void add_Success() throws Exception {
            SysUser user = createUser(null, "newUser", "新用户");
            user.setPassword("123456");
            doNothing().when(sysUserService).createUser(any(SysUser.class));

            mockMvc.perform(post("/system/user")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("修改用户")
    class UpdateTests {

        @Test
        @DisplayName("正常修改")
        void update_Success() throws Exception {
            SysUser user = createUser(1L, "admin", "管理员更新");
            doNothing().when(sysUserService).updateUser(any(SysUser.class));

            mockMvc.perform(put("/system/user")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("修改时不允许传密码")
        void update_WithPassword() throws Exception {
            SysUser user = createUser(1L, "admin", "管理员");
            user.setPassword("newPassword");

            mockMvc.perform(put("/system/user")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(ResultCode.PARAM_VALID_ERROR.getCode()));
        }
    }

    @Nested
    @DisplayName("删除用户")
    class DeleteTests {

        @Test
        @DisplayName("单个删除")
        void delete_Success() throws Exception {
            when(sysUserService.removeById(1L)).thenReturn(true);

            mockMvc.perform(delete("/system/user/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("批量删除")
        void deleteBatch_Success() throws Exception {
            when(sysUserService.removeByIds(any(List.class))).thenReturn(true);

            mockMvc.perform(delete("/system/user/batch")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(List.of(1L, 2L))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("重置密码")
    class ResetPasswordTests {

        @Test
        @DisplayName("正常重置")
        void resetPassword_Success() throws Exception {
            when(sysUserService.resetPassword(1L, "newPass123")).thenReturn(true);

            mockMvc.perform(put("/system/user/reset-pwd")
                            .param("userId", "1")
                            .param("password", "newPass123"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(sysUserService).resetPassword(1L, "newPass123");
        }
    }

    @Nested
    @DisplayName("修改状态")
    class ChangeStatusTests {

        @Test
        @DisplayName("正常修改状态")
        void changeStatus_Success() throws Exception {
            when(sysUserService.changeStatus(1L, 1)).thenReturn(true);

            mockMvc.perform(put("/system/user/change-status")
                            .param("userId", "1")
                            .param("status", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(sysUserService).changeStatus(1L, 1);
        }
    }
}