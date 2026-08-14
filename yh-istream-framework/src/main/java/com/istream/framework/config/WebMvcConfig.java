package com.istream.framework.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
                    // 排除登录、注册、验证码等接口
                    SaRouter.match("/**")
                            .notMatch(
                                    "/auth/login",
                                    "/auth/register",
                                    "/auth/captcha",
                                    "/doc.html",
                                    "/webjars/**",
                                    "/v3/api-docs/**",
                                    "/knife4j/**",
                                    "/favicon.ico",
                                    "/error"
                            )
                            .check(r -> StpUtil.checkLogin());
                }))
                .addPathPatterns("/**");
    }
}