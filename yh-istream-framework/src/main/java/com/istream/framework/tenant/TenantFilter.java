package com.istream.framework.tenant;

import cn.dev33.satoken.stp.StpUtil;
import com.istream.common.constant.Constants;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 租户上下文过滤器
 *
 * <p>在请求进入时从 Sa-Token 会话中提取租户ID并设置到 {@link TenantContext}，
 * 请求结束后自动清理，防止 ThreadLocal 泄漏。</p>
 * <p>执行顺序在 Sa-Token 过滤器之后（@Order(200)），确保已登录状态可用。</p>
 *
 * @author istream
 * @since 2026-09-08
 */
@Slf4j
@Component
@Order(200)
public class TenantFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            if (StpUtil.isLogin()) {
                Object tenantId = StpUtil.getSession().get(Constants.SESSION_TENANT_KEY);
                if (tenantId instanceof Long tid) {
                    TenantContext.setTenantId(tid);
                }
            }
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}