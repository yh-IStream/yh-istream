package com.istream.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.istream.system.entity.SysOperLog;
import com.istream.system.service.SysOperLogService;
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

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SysOperLogController 接口测试")
class SysOperLogControllerTest {

    @Mock
    private SysOperLogService sysOperLogService;

    @InjectMocks
    private SysOperLogController sysOperLogController;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(sysOperLogController).build();
    }

    private SysOperLog createOperLog(Long id, String title, int status) {
        SysOperLog log = new SysOperLog();
        log.setId(id);
        log.setTitle(title);
        log.setBusinessType(1);
        log.setMethod("com.istream.controller.SysUserController.add");
        log.setRequestMethod("POST");
        log.setOperUrl("/system/user");
        log.setOperIp("127.0.0.1");
        log.setStatus(status);
        log.setCostTime(50L);
        log.setOperName("admin");
        log.setOperTime(LocalDateTime.now());
        return log;
    }

    @Nested
    @DisplayName("分页查询")
    class ListTests {

        @Test
        @DisplayName("正常查询")
        void list_Success() throws Exception {
            mockMvc.perform(get("/monitor/oper-log/list"))
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
            SysOperLog log = createOperLog(1L, "用户管理", 0);
            when(sysOperLogService.getById(1L)).thenReturn(log);

            mockMvc.perform(get("/monitor/oper-log/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.title").value("用户管理"));
        }

        @Test
        @DisplayName("ID不存在")
        void getById_NotFound() throws Exception {
            when(sysOperLogService.getById(999L)).thenReturn(null);

            mockMvc.perform(get("/monitor/oper-log/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    @DisplayName("删除操作日志")
    class DeleteTests {

        @Test
        @DisplayName("正常删除")
        void delete_Success() throws Exception {
            when(sysOperLogService.removeById(1L)).thenReturn(true);

            mockMvc.perform(delete("/monitor/oper-log/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("批量删除")
        void deleteBatch_Success() throws Exception {
            when(sysOperLogService.removeByIds(anyList())).thenReturn(true);

            mockMvc.perform(delete("/monitor/oper-log/batch")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(List.of(1L, 2L, 3L))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("清空操作日志")
    class ClearTests {

        @Test
        @DisplayName("正常清空")
        void clear_Success() throws Exception {
            mockMvc.perform(delete("/monitor/oper-log/clear"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }
}