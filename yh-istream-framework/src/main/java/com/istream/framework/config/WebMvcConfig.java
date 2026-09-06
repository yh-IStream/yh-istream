package com.istream.framework.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

/**
 * Web MVC 配置
 *
 * <p>包含 CORS 跨域、Sa-Token 鉴权拦截、静态资源映射。
 * 鉴权白名单路径通过 {@code sa-token.whitelist} 配置项外部化，
 * 便于不同环境灵活调整。</p>
 *
 * @author istream
 * @since 2026-08-17
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${oss.local.upload-path:./uploads}")
    private String uploadPath;

    @Value("${cors.allowed-origins:*}")
    private String allowedOrigins;

    @Value("${sa-token.whitelist:}")
    private String customWhitelist;

    private static final List<String> DEFAULT_WHITELIST = List.of(
            "/auth/login",
            "/auth/captcha",
            "/system/config/key/**",
            "/sse/**",
            "/doc.html",
            "/webjars/**",
            "/v3/api-docs/**",
            "/knife4j/**",
            "/favicon.ico",
            "/error",
            "/files/**"
    );

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(allowedOrigins.split(","))
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        List<String> whitelist = buildWhitelist();

        registry.addInterceptor(new SaInterceptor(handle -> {
                    SaRouter.match("/**")
                            .notMatch(whitelist)
                            .check(r -> StpUtil.checkLogin());
                }))
                .addPathPatterns("/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }

    /**
     * 构建鉴权白名单：默认白名单 + 配置文件自定义白名单
     */
    private List<String> buildWhitelist() {
        List<String> whitelist = new ArrayList<>(DEFAULT_WHITELIST);
        if (customWhitelist != null && !customWhitelist.isBlank()) {
            for (String path : customWhitelist.split(",")) {
                String trimmed = path.trim();
                if (!trimmed.isEmpty()) {
                    whitelist.add(trimmed);
                }
            }
        }
        return whitelist;
    }
}