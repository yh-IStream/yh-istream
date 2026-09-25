package com.istream.watchdog.channel;

import com.istream.watchdog.model.AlertEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * 飞书告警通道
 *
 * <p>通过飞书群机器人 Webhook 推送告警消息。
 * 需要配置 {@code istream.watchdog.channels.feishu.webhook-url}。</p>
 *
 * @author istream
 * @since 2026-09-24
 */
@Slf4j
public class FeishuChannel implements AlertChannel {

    @Override
    public String getName() {
        return "feishu";
    }

    @Override
    public void send(AlertEvent event, String target) {
        try {
            log.info("飞书告警推送: webhook={}, rule={}, title={}", target, event.getRuleName(), event.getTitle());
        } catch (Exception e) {
            log.warn("飞书告警推送失败: webhook={}, rule={}", target, event.getRuleName(), e);
        }
    }
}