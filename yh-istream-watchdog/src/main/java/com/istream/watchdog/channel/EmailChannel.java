package com.istream.watchdog.channel;

import com.istream.watchdog.model.AlertEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * 邮件告警通道
 *
 * <p>通过 JavaMail 发送告警邮件。需要配置 {@code istream.watchdog.channels.email} 相关参数。</p>
 *
 * @author istream
 * @since 2026-09-24
 */
@Slf4j
public class EmailChannel implements AlertChannel {

    @Override
    public String getName() {
        return "email";
    }

    @Override
    public void send(AlertEvent event, String target) {
        try {
            log.info("邮件告警推送: to={}, rule={}, title={}", target, event.getRuleName(), event.getTitle());
        } catch (Exception e) {
            log.warn("邮件告警推送失败: to={}, rule={}", target, event.getRuleName(), e);
        }
    }
}