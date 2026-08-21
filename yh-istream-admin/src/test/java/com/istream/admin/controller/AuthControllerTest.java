package com.istream.admin.controller;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.istream.common.enums.ResultCode;
import com.istream.common.enums.StatusEnum;
import com.istream.common.model.dto.LoginDTO;
import com.istream.common.model.sse.SseEvent;
import com.istream.framework.sse.SseService;
import com.istream.framework.security.SecurityUtils;
import com.istream.system.entity.SysMenu;
import com.istream.system.entity.SysUser;
import com.istream.system.service.SysLoginInfoService;
import com.istream.system.service.SysMenuService;
import com.istream.system.service.SysUserService;
import jakarta.servlet.http.HttpServletRequest;
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
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AuthController 接口测试")
class AuthControllerTest {

    @Mock
    private SysUserService sysUserService;
    @Mock
    private SysMenuService sysMenuService;
    @Mock
    private SysLoginInfoService sysLoginInfoService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private RedissonClient redissonClient;
    @Mock
    private SseService sseService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockedStatic<StpUtil> stpUtilMock;
    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        stpUtilMock = mockStatic(StpUtil.class);
        securityUtilsMock = mockStatic(SecurityUtils.class);
    }

    @AfterEach
    void tearDown() {
        if (stpUtilMock != null) {
            stpUtilMock.close();
        }
        if (securityUtilsMock != null) {
            securityUtilsMock.close();
        }
    }

    private SysUser createUser(Long id, String username, String encodedPassword) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setUsername(username);
        user.setPassword(encodedPassword);
        user.setNickname(username);
        user.setStatus(0);
        return user;
    }

    @Nested
    @DisplayName("用户登录")
    class LoginTests {

        @Test
        @DisplayName("登录成功")
        void login_Success() throws Exception {
            LoginDTO loginDTO = new LoginDTO();
            loginDTO.setUsername("admin");
            loginDTO.setPassword("123456");

            SysUser user = createUser(1L, "admin", BCrypt.hashpw("123456", BCrypt.gensalt()));

            when(sysUserService.getByUsername("admin")).thenReturn(user);
            doNothing().when(sysUserService).updateLoginFailCount(anyLong(), eq(0));

            RBucket<Object> bucket = mock(RBucket.class);
            doReturn(bucket).when(redissonClient).getBucket(anyString());

            RAtomicLong failCount = mock(RAtomicLong.class);
            when(failCount.get()).thenReturn(0L);
            when(redissonClient.getAtomicLong(anyString())).thenReturn(failCount);

            stpUtilMock.when(() -> StpUtil.getTokenValue()).thenReturn("mock-token-value");
            stpUtilMock.when(() -> StpUtil.getTokenName()).thenReturn("Authorization");
            SaSession session = mock(SaSession.class);
            stpUtilMock.when(() -> StpUtil.getSession()).thenReturn(session);

            doNothing().when(sseService).broadcast(any(SseEvent.class));

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.token").value("mock-token-value"))
                    .andExpect(jsonPath("$.data.tokenName").value("Authorization"));
        }

        @Test
        @DisplayName("用户不存在")
        void login_UserNotFound() throws Exception {
            LoginDTO loginDTO = new LoginDTO();
            loginDTO.setUsername("ghost");
            loginDTO.setPassword("123456");

            when(sysUserService.getByUsername("ghost")).thenReturn(null);

            RAtomicLong failCount = mock(RAtomicLong.class);
            when(failCount.get()).thenReturn(0L);
            when(failCount.incrementAndGet()).thenReturn(1L);
            when(redissonClient.getAtomicLong(anyString())).thenReturn(failCount);

            doNothing().when(sseService).broadcast(any(SseEvent.class));

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(ResultCode.USER_NOT_EXIST.getCode()));
        }

        @Test
        @DisplayName("用户已被停用")
        void login_UserDisabled() throws Exception {
            LoginDTO loginDTO = new LoginDTO();
            loginDTO.setUsername("disabled");
            loginDTO.setPassword("123456");

            SysUser user = createUser(2L, "disabled", "encoded");
            user.setStatus(StatusEnum.DISABLED.getCode());

            when(sysUserService.getByUsername("disabled")).thenReturn(user);

            RAtomicLong failCount = mock(RAtomicLong.class);
            when(failCount.get()).thenReturn(0L);
            when(redissonClient.getAtomicLong(anyString())).thenReturn(failCount);

            doNothing().when(sseService).broadcast(any(SseEvent.class));

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(ResultCode.USER_DISABLED.getCode()));
        }
    }

    @Nested
    @DisplayName("用户登出")
    class LogoutTests {

        @Test
        @DisplayName("正常登出")
        void logout_Success() throws Exception {
            mockMvc.perform(post("/auth/logout"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            stpUtilMock.verify(StpUtil::logout);
        }
    }

    @Nested
    @DisplayName("获取验证码")
    class CaptchaTests {

        @Test
        @DisplayName("正常获取验证码")
        void captcha_Success() throws Exception {
            RBucket<Object> bucket = mock(RBucket.class);
            doReturn(bucket).when(redissonClient).getBucket(anyString());

            mockMvc.perform(get("/auth/captcha"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.uuid").isString())
                    .andExpect(jsonPath("$.data.image").isString());
        }
    }

    @Nested
    @DisplayName("获取当前用户信息")
    class GetUserInfoTests {

        @Test
        @DisplayName("正常获取")
        void getUserInfo_Success() throws Exception {
            securityUtilsMock.when(SecurityUtils::getLoginUserId).thenReturn(1L);

            SysUser user = createUser(1L, "admin", "encoded");
            when(sysUserService.getById(1L)).thenReturn(user);
            when(sysMenuService.getPermissionsByUserId(1L)).thenReturn(List.of("system:user:list"));
            when(sysMenuService.listMenuTree()).thenReturn(List.of(new SysMenu()));

            mockMvc.perform(get("/auth/user-info"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.user.username").value("admin"))
                    .andExpect(jsonPath("$.data.permissions[0]").value("system:user:list"))
                    .andExpect(jsonPath("$.data.menus").isArray());
        }

        @Test
        @DisplayName("用户不存在")
        void getUserInfo_NotFound() throws Exception {
            securityUtilsMock.when(SecurityUtils::getLoginUserId).thenReturn(999L);
            when(sysUserService.getById(999L)).thenReturn(null);

            mockMvc.perform(get("/auth/user-info"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(ResultCode.USER_NOT_EXIST.getCode()));
        }
    }
}