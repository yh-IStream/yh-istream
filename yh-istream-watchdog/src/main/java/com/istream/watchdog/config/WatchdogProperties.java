package com.istream.watchdog.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Watchdog 智能守护配置
 *
 * <p>对应 YAML 配置前缀 {@code istream.watchdog}，支持按需开启/关闭整个 Watchdog 模块。</p>
 *
 * <p>配置示例：</p>
 * <pre>{@code
 * istream:
 *   watchdog:
 *     enabled: true
 *     channels:
 *       sse:
 *         enabled: true
 *       email:
 *         enabled: true
 *         from: alert@istream.com
 *       wecom:
 *         enabled: true
 *         webhook-url: https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=xxx
 *       dingtalk:
 *         enabled: false
 *         webhook-url: https://oapi.dingtalk.com/robot/send?access_token=xxx
 *       feishu:
 *         enabled: false
 *         webhook-url: https://open.feishu.cn/open-apis/bot/v2/hook/xxx
 *     escalation:
 *       - channel: sse
 *         timeout: 5m
 *       - channel: email
 *         timeout: 30m
 *       - channel: wecom
 *         timeout: 2h
 *       - channel: sms
 *         timeout: 0
 *     aggregate-window: 10m
 * }</pre>
 *
 * @author istream
 * @since 2026-09-24
 */
@Data
@Component
@ConfigurationProperties(prefix = "istream.watchdog")
public class WatchdogProperties {

    /** 是否启用 Watchdog 模块（默认关闭，渐进式架构 L2） */
    private boolean enabled = false;

    /** 告警通道配置 */
    private Channels channels = new Channels();

    /** 升级策略配置（按优先级排列，timeout 为当前通道超时时间） */
    private List<EscalationStep> escalation = List.of();

    /** 告警聚合窗口（默认 10 分钟内同类告警合并为 1 条） */
    private String aggregateWindow = "10m";

    /**
     * 告警通道配置
     */
    @Data
    public static class Channels {
        private ChannelConfig sse = new ChannelConfig();
        private EmailChannelConfig email = new EmailChannelConfig();
        private WebhookChannelConfig wecom = new WebhookChannelConfig();
        private WebhookChannelConfig dingtalk = new WebhookChannelConfig();
        private WebhookChannelConfig feishu = new WebhookChannelConfig();
    }

    @Data
    public static class ChannelConfig {
        private boolean enabled = false;
    }

    @Data
    public static class EmailChannelConfig extends ChannelConfig {
        /** 发件人地址 */
        private String from;
    }

    @Data
    public static class WebhookChannelConfig extends ChannelConfig {
        /** Webhook URL */
        private String webhookUrl;
    }

    /**
     * 升级策略步骤
     */
    @Data
    public static class EscalationStep {
        /** 通道名称（sse / email / wecom / sms） */
        private String channel;
        /** 当前通道超时时间（如 5m、30m、2h），0 表示无超时（最后一级） */
        private String timeout;
    }
}