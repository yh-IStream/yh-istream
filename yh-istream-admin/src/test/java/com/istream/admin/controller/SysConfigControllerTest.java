package com.istream.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.istream.common.enums.ResultCode;
import com.istream.system.entity.SysConfig;
import com.istream.system.service.SysConfigService;
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
@DisplayName("SysConfigController 接口测试")
class SysConfigControllerTest {

    @Mock
    private SysConfigService sysConfigService;

    @InjectMocks
    private SysConfigController sysConfigController;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(sysConfigController).build();
    }

    private SysConfig createConfig(Long id, String configKey, String configValue) {
        SysConfig config = new SysConfig();
        config.setId(id);
        config.setConfigKey(configKey);
        config.setConfigValue(configValue);
        return config;
    }

    @Nested
    @DisplayName("分页查询")
    class ListTests {

        @Test
        @DisplayName("正常查询，返回配置列表")
        void list_Success() throws Exception {
            doAnswer(invocation -> {
                Page<SysConfig> page = invocation.getArgument(0);
                page.setRecords(List.of(
                        createConfig(1L, "sys.app.name", "iStream"),
                        createConfig(2L, "sys.app.version", "1.0.0")));
                page.setTotal(2);
                return null;
            }).when(sysConfigService).page(any(Page.class), any(LambdaQueryWrapper.class));

            mockMvc.perform(get("/system/config/list"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records.length()").value(2))
                    .andExpect(jsonPath("$.data.total").value(2))
                    .andExpect(jsonPath("$.data.records[0].configKey").value("sys.app.name"));
        }
    }

    @Nested
    @DisplayName("根据配置键查询")
    class GetByKeyTests {

        @Test
        @DisplayName("配置存在")
        void getByKey_Success() throws Exception {
            when(sysConfigService.getConfigValueByKey("sys.upload.maxSize")).thenReturn("10MB");

            mockMvc.perform(get("/system/config/key/sys.upload.maxSize"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value("10MB"));
        }

        @Test
        @DisplayName("配置不存在")
        void getByKey_NotFound() throws Exception {
            when(sysConfigService.getConfigValueByKey("unknown.key")).thenReturn(null);

            mockMvc.perform(get("/system/config/key/unknown.key"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    @DisplayName("新增配置")
    class AddTests {

        @Test
        @DisplayName("正常新增")
        void add_Success() throws Exception {
            SysConfig config = createConfig(null, "sys.upload.maxSize", "10MB");
            when(sysConfigService.count(any(LambdaQueryWrapper.class))).thenReturn(0L);
            when(sysConfigService.save(any(SysConfig.class))).thenReturn(true);

            mockMvc.perform(post("/system/config")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(config)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("配置键已存在")
        void add_DuplicateKey() throws Exception {
            SysConfig config = createConfig(null, "sys.upload.maxSize", "10MB");
            when(sysConfigService.count(any(LambdaQueryWrapper.class))).thenReturn(1L);

            mockMvc.perform(post("/system/config")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(config)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(ResultCode.DATA_DUPLICATE.getCode()));
        }
    }

    @Nested
    @DisplayName("修改配置")
    class UpdateTests {

        @Test
        @DisplayName("配置键重复（不同记录）")
        void update_DuplicateKey() throws Exception {
            SysConfig config = createConfig(1L, "sys.user.initPassword", "newValue");
            when(sysConfigService.count(any(LambdaQueryWrapper.class))).thenReturn(1L);

            mockMvc.perform(put("/system/config")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(config)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(ResultCode.DATA_DUPLICATE.getCode()));
        }

        @Test
        @DisplayName("正常修改")
        void update_Success() throws Exception {
            SysConfig config = createConfig(1L, "sys.upload.maxSize", "20MB");
            when(sysConfigService.count(any(LambdaQueryWrapper.class))).thenReturn(0L);
            when(sysConfigService.updateById(any(SysConfig.class))).thenReturn(true);

            mockMvc.perform(put("/system/config")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(config)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("删除配置")
    class DeleteTests {

        @Test
        @DisplayName("正常删除")
        void delete_Success() throws Exception {
            when(sysConfigService.removeById(1L)).thenReturn(true);

            mockMvc.perform(delete("/system/config/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }
}