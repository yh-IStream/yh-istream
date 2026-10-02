package com.istream.watchdog.channel;

import com.istream.watchdog.model.AlertEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * 邮件告警通道
 *
 * <p>通过 JavaMail 发送告警邮件。
 * 需要配置 {@code istream.watchdog.channels.email} 相关参数
 * 以及 Spring Boot Mail 标准配置（{@code spring.mail.*}）。</p>
 *
 * @author istream
 * @since 2026-09-24
 */
@Slf4j
@RequiredArgsConstructor
public class EmailChannel implements AlertChannel {

    private final JavaMailSender mailSender;
    private final String from;

    @Override
    public String getName() {
        return "email";
    }

    @Override
    public void send(AlertEvent event, String target) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(target);
            message.setSubject("⚠️ " + event.getTitle());
            message.setText(event.getContent());
            mailSender.send(message);

            log.info("邮件告警推送成功: to={}, rule={}", target, event.getRuleName());
        } catch (Exception e) {
            log.warn("邮件告警推送失败: to={}, rule={}", target, event.getRuleName(), e);
        }
    }
}