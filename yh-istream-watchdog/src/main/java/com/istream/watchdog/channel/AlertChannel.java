package com.istream.watchdog.channel;

import com.istream.watchdog.model.AlertEvent;

/**
 * 告警推送通道
 *
 * <p>所有告警通道的统一接口，支持 SSE、邮件、企业微信、钉钉、飞书、SMS 等。</p>
 * <p>每个通道实现负责将告警事件推送到对应的目标，推送失败只 warn 不抛异常。</p>
 *
 * @author istream
 * @since 2026-09-24
 */
public interface AlertChannel {

    /**
     * 通道名称（如 sse、email、wecom、dingtalk、feishu、sms）
     *
     * @return 通道名称
     */
    String getName();

    /**
     * 推送告警
     *
     * @param event  告警事件
     * @param target 推送目标（用户ID / 邮箱 / 手机号 / Webhook URL）
     */
    void send(AlertEvent event, String target);
}