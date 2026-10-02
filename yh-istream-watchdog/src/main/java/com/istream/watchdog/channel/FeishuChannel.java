package com.istream.watchdog.channel;

import com.istream.watchdog.model.AlertEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * 飞书告警通道
 *
 * <p>通过飞书群机器人 Webhook 推送告警消息。
 * 需要配置 {@code istream.watchdog.channels.feishu.webhook-url}。</p>
 *
 * <p>飞书 Webhook 消息格式参考：
 * <a href="https://open.feishu.cn/document/ukTMukTMukTM/ucTM5YjL3ETO24yNxkjN">飞书开放平台</a></p>
 *
 * @author istream
 * @since 2026-09-24
 */
@Slf4j
@RequiredArgsConstructor
public class FeishuChannel implements AlertChannel {

    private final RestClient restClient;

    @Override
    public String getName() {
        return "feishu";
    }

    @Override
    public void send(AlertEvent event, String target) {
        try {
            Map<String, Object> message = Map.of(
                    "msg_type", "interactive",
                    "card", Map.of(
                            "header", Map.of(
                                    "title", Map.of(
                                            "tag", "plain_text",
                                            "content", "⚠️ " + event.getTitle()
                                    ),
                                    "template", severityColor(event.getSeverity())
                            ),
                            "elements", List.of(
                                    Map.of("tag", "div", "text", Map.of(
                                            "tag", "plain_text",
                                            "content", event.getContent()
                                    ))
                            )
                    )
            );

            restClient.post()
                    .uri(target)
                    .body(message)
                    .retrieve()
                    .body(String.class);

            log.info("飞书告警推送成功: webhook={}, rule={}", target, event.getRuleName());
        } catch (Exception e) {
            log.warn("飞书告警推送失败: webhook={}, rule={}", target, event.getRuleName(), e);
        }
    }

    private String severityColor(int severity) {
        return switch (severity) {
            case 4 -> "red";
            case 3 -> "orange";
            case 2 -> "yellow";
            default -> "blue";
        };
    }
}