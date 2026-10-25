package com.istream.framework.config;

import com.istream.framework.tenant.TenantContext;
import org.springframework.core.task.TaskDecorator;

/**
 * 上下文传播任务装饰器
 *
 * <p>在任务提交到异步线程池时，捕获当前线程的 ThreadLocal 上下文（租户ID等），
 * 在异步线程执行前恢复，执行后清理，确保异步任务能正确访问多租户等线程上下文。</p>
 *
 * <p>使用方式：在 {@link AsyncConfig} 中设置到 {@code ThreadPoolTaskExecutor.setTaskDecorator()}。</p>
 *
 * @author istream
 * @since 2026-10-24
 */
public class ContextPropagatingTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        Long tenantId = TenantContext.getTenantId();

        return () -> {
            TenantContext.setTenantId(tenantId);
            try {
                runnable.run();
            } finally {
                TenantContext.clear();
            }
        };
    }
}