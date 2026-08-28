package com.istream.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.istream.system.entity.SysLoginInfo;
import com.istream.system.service.SysLoginInfoService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SysLoginInfoController 接口测试")
class SysLoginInfoControllerTest {

    @Mock
    private SysLoginInfoService sysLoginInfoService;

    @InjectMocks
    private SysLoginInfoController sysLoginInfoController;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(sysLoginInfoController).build();
    }

    private SysLoginInfo createLoginInfo(Long id, String username, int status) {
        SysLoginInfo info = new SysLoginInfo();
        info.setId(id);
        info.setUsername(username);
        info.setIpAddress("127.0.0.1");
        info.setLoginLocation("本地");
        info.setStatus(status);
        info.setMsg(status == 0 ? "登录成功" : "登录失败");
        info.setLoginTime(LocalDateTime.now());
        return info;
    }

    @Nested
    @DisplayName("分页查询")
    class ListTests {

        @Test
        @DisplayName("正常查询")
        void list_Success() throws Exception {
            mockMvc.perform(get("/monitor/login-info/list"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").exists());
        }
    }

    @Nested
    @DisplayName("根据ID查询")
    class GetByIdTests {

        @Test
        @DisplayName("正常查询")
        void getById_Success() throws Exception {
            SysLoginInfo info = createLoginInfo(1L, "admin", 0);
            when(sysLoginInfoService.getById(1L)).thenReturn(info);

            mockMvc.perform(get("/monitor/login-info/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.username").value("admin"));
        }

        @Test
        @DisplayName("ID不存在")
        void getById_NotFound() throws Exception {
            when(sysLoginInfoService.getById(999L)).thenReturn(null);

            mockMvc.perform(get("/monitor/login-info/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    @DisplayName("删除登录日志")
    class DeleteTests {

        @Test
        @DisplayName("正常删除")
        void delete_Success() throws Exception {
            when(sysLoginInfoService.removeById(1L)).thenReturn(true);

            mockMvc.perform(delete("/monitor/login-info/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("批量删除")
        void deleteBatch_Success() throws Exception {
            when(sysLoginInfoService.removeByIds(anyList())).thenReturn(true);

            mockMvc.perform(delete("/monitor/login-info/batch")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(List.of(1L, 2L, 3L))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("清空登录日志")
    class ClearTests {

        @Test
        @DisplayName("正常清空")
        void clear_Success() throws Exception {
            mockMvc.perform(delete("/monitor/login-info/clear"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }
}