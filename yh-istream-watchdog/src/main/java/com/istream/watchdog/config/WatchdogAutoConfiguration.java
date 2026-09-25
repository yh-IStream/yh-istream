package com.istream.watchdog.config;

import com.istream.framework.sse.SseService;
import com.istream.watchdog.channel.EmailChannel;
import com.istream.watchdog.channel.FeishuChannel;
import com.istream.watchdog.channel.SseAlertChannel;
import com.istream.watchdog.channel.WeComChannel;
import com.istream.watchdog.channel.DingTalkChannel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Watchdog 自动配置
 *
 * <p>仅当 {@code istream.watchdog.enabled=true} 时注册相关 Bean，
 * 关闭时零开销（渐进式架构 L2 可拆卸）。</p>
 *
 * @author istream
 * @since 2026-09-24
 */
@Configuration
@ConditionalOnProperty(prefix = "istream.watchdog", name = "enabled", havingValue = "true")
public class WatchdogAutoConfiguration {

    @Bean
    public SseAlertChannel sseAlertChannel(SseService sseService) {
        return new SseAlertChannel(sseService);
    }

    @Bean
    @ConditionalOnProperty(prefix = "istream.watchdog.channels.email", name = "enabled", havingValue = "true")
    public EmailChannel emailChannel() {
        return new EmailChannel();
    }

    @Bean
    @ConditionalOnProperty(prefix = "istream.watchdog.channels.wecom", name = "enabled", havingValue = "true")
    public WeComChannel weComChannel() {
        return new WeComChannel();
    }

    @Bean
    @ConditionalOnProperty(prefix = "istream.watchdog.channels.dingtalk", name = "enabled", havingValue = "true")
    public DingTalkChannel dingTalkChannel() {
        return new DingTalkChannel();
    }

    @Bean
    @ConditionalOnProperty(prefix = "istream.watchdog.channels.feishu", name = "enabled", havingValue = "true")
    public FeishuChannel feishuChannel() {
        return new FeishuChannel();
    }
}