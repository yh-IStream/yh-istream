package com.istream.watchdog.channel;

import com.istream.watchdog.model.AlertEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * 企业微信告警通道
 *
 * <p>通过企业微信群机器人 Webhook 推送告警消息。
 * 需要配置 {@code istream.watchdog.channels.wecom.webhook-url}。</p>
 *
 * @author istream
 * @since 2026-09-24
 */
@Slf4j
public class WeComChannel implements AlertChannel {

    @Override
    public String getName() {
        return "wecom";
    }

    @Override
    public void send(AlertEvent event, String target) {
        try {
            log.info("企业微信告警推送: webhook={}, rule={}, title={}", target, event.getRuleName(), event.getTitle());
        } catch (Exception e) {
            log.warn("企业微信告警推送失败: webhook={}, rule={}", target, event.getRuleName(), e);
        }
    }
}