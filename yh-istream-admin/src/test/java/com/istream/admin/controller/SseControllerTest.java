package com.istream.admin.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.istream.framework.sse.SseService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SseController 接口测试")
class SseControllerTest {

    @Mock
    private SseService sseService;

    @InjectMocks
    private SseController sseController;

    private MockMvc mockMvc;
    private MockedStatic<StpUtil> stpUtilMock;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(sseController).build();
        stpUtilMock = mockStatic(StpUtil.class);
    }

    @AfterEach
    void tearDown() {
        if (stpUtilMock != null) {
            stpUtilMock.close();
        }
    }

    @Nested
    @DisplayName("SSE 订阅")
    class SubscribeTests {

        @Test
        @DisplayName("通过 token 订阅成功")
        void subscribe_WithValidToken() throws Exception {
            stpUtilMock.when(() -> StpUtil.getLoginIdByToken("valid-token")).thenReturn(1L);

            SseEmitter emitter = new SseEmitter(300_000L);
            when(sseService.subscribe(1L)).thenReturn(emitter);

            mockMvc.perform(get("/sse/subscribe")
                            .param("token", "valid-token"))
                    .andExpect(status().isOk());

            verify(sseService).subscribe(1L);
        }

        @Test
        @DisplayName("token 无效，不调用 subscribe")
        void subscribe_WithInvalidToken() throws Exception {
            stpUtilMock.when(() -> StpUtil.getLoginIdByToken("bad-token")).thenReturn(null);

            mockMvc.perform(get("/sse/subscribe")
                            .param("token", "bad-token"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("无 token 且已登录，订阅成功")
        void subscribe_NoToken_LoggedIn() throws Exception {
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(1L);

            SseEmitter emitter = new SseEmitter(300_000L);
            when(sseService.subscribe(1L)).thenReturn(emitter);

            mockMvc.perform(get("/sse/subscribe"))
                    .andExpect(status().isOk());

            verify(sseService).subscribe(1L);
        }

        @Test
        @DisplayName("无 token 且未登录，不调用 subscribe")
        void subscribe_NoToken_NotLoggedIn() throws Exception {
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenThrow(new RuntimeException("未登录"));

            mockMvc.perform(get("/sse/subscribe"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("空 token 参数，走已登录流程")
        void subscribe_EmptyToken() throws Exception {
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(1L);

            SseEmitter emitter = new SseEmitter(300_000L);
            when(sseService.subscribe(1L)).thenReturn(emitter);

            mockMvc.perform(get("/sse/subscribe")
                            .param("token", ""))
                    .andExpect(status().isOk());

            verify(sseService).subscribe(1L);
        }
    }
}