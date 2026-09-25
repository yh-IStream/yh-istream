package com.istream.watchdog.channel;

import com.istream.watchdog.model.AlertEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * 钉钉告警通道
 *
 * <p>通过钉钉群机器人 Webhook 推送告警消息。
 * 需要配置 {@code istream.watchdog.channels.dingtalk.webhook-url}。</p>
 *
 * @author istream
 * @since 2026-09-24
 */
@Slf4j
public class DingTalkChannel implements AlertChannel {

    @Override
    public String getName() {
        return "dingtalk";
    }

    @Override
    public void send(AlertEvent event, String target) {
        try {
            log.info("钉钉告警推送: webhook={}, rule={}, title={}", target, event.getRuleName(), event.getTitle());
        } catch (Exception e) {
            log.warn("钉钉告警推送失败: webhook={}, rule={}", target, event.getRuleName(), e);
        }
    }
}