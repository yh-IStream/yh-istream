package com.istream.watchdog.channel;

import com.istream.watchdog.model.AlertEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * 企业微信告警通道
 *
 * <p>通过企业微信群机器人 Webhook 推送告警消息。
 * 需要配置 {@code istream.watchdog.channels.wecom.webhook-url}。</p>
 *
 * <p>企业微信 Webhook 消息格式参考：
 * <a href="https://developer.work.weixin.qq.com/document/path/91770">企业微信开发文档</a></p>
 *
 * @author istream
 * @since 2026-09-24
 */
@Slf4j
@RequiredArgsConstructor
public class WeComChannel implements AlertChannel {

    private final RestClient restClient;

    @Override
    public String getName() {
        return "wecom";
    }

    @Override
    public void send(AlertEvent event, String target) {
        try {
            Map<String, Object> message = Map.of(
                    "msgtype", "markdown",
                    "markdown", Map.of(
                            "content", "### ⚠️ " + event.getTitle() + "\n> " + event.getContent()
                    )
            );

            restClient.post()
                    .uri(target)
                    .body(message)
                    .retrieve()
                    .body(String.class);

            log.info("企业微信告警推送成功: webhook={}, rule={}", target, event.getRuleName());
        } catch (Exception e) {
            log.warn("企业微信告警推送失败: webhook={}, rule={}", target, event.getRuleName(), e);
        }
    }
}