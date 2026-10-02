package com.istream.watchdog.channel;

import com.istream.watchdog.model.AlertEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * 钉钉告警通道
 *
 * <p>通过钉钉群机器人 Webhook 推送告警消息。
 * 需要配置 {@code istream.watchdog.channels.dingtalk.webhook-url}。</p>
 *
 * <p>钉钉 Webhook 消息格式参考：
 * <a href="https://open.dingtalk.com/document/robots/custom-robot-access">钉钉开放平台</a></p>
 *
 * @author istream
 * @since 2026-09-24
 */
@Slf4j
@RequiredArgsConstructor
public class DingTalkChannel implements AlertChannel {

    private final RestClient restClient;

    @Override
    public String getName() {
        return "dingtalk";
    }

    @Override
    public void send(AlertEvent event, String target) {
        try {
            Map<String, Object> message = Map.of(
                    "msgtype", "markdown",
                    "markdown", Map.of(
                            "title", event.getTitle(),
                            "text", "### ⚠️ " + event.getTitle() + "\n\n" + event.getContent()
                    )
            );

            restClient.post()
                    .uri(target)
                    .body(message)
                    .retrieve()
                    .body(String.class);

            log.info("钉钉告警推送成功: webhook={}, rule={}", target, event.getRuleName());
        } catch (Exception e) {
            log.warn("钉钉告警推送失败: webhook={}, rule={}", target, event.getRuleName(), e);
        }
    }
}