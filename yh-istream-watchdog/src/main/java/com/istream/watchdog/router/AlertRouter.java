package com.istream.watchdog.router;

import com.istream.watchdog.channel.AlertChannel;
import com.istream.watchdog.config.WatchdogProperties;
import com.istream.watchdog.model.AlertEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 告警路由器
 *
 * <p>根据告警事件的严重级别、接收人、通道配置，将告警路由到指定通道推送。</p>
 *
 * <h3>路由逻辑</h3>
 * <ol>
 *   <li>如果告警指定了 channels，按指定通道推送</li>
 *   <li>否则按严重级别选择默认通道：低→SSE, 中→SSE+邮件, 高→SSE+邮件+企业微信, 紧急→全通道</li>
 * </ol>
 *
 * @author istream
 * @since 2026-09-24
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "istream.watchdog", name = "enabled", havingValue = "true")
public class AlertRouter {

    private final Map<String, AlertChannel> channelMap;
    private final WatchdogProperties properties;

    public AlertRouter(List<AlertChannel> channels, WatchdogProperties properties) {
        this.channelMap = channels.stream()
                .collect(Collectors.toMap(AlertChannel::getName, Function.identity(), (a, b) -> a));
        this.properties = properties;
    }

    /**
     * 路由并推送告警
     *
     * @param event 告警事件
     */
    public void route(AlertEvent event) {
        List<String> targetChannels = resolveChannels(event);

        for (String channelName : targetChannels) {
            AlertChannel channel = channelMap.get(channelName);
            if (channel == null) {
                log.warn("AlertRouter: 通道 {} 未注册，跳过", channelName);
                continue;
            }

            List<String> targets = resolveTargets(channelName, event);
            for (String target : targets) {
                channel.send(event, target);
            }
        }
    }

    private List<String> resolveChannels(AlertEvent event) {
        if (event.getChannels() != null && !event.getChannels().isEmpty()) {
            return event.getChannels();
        }

        return switch (event.getSeverity()) {
            case 1 -> List.of("sse");
            case 2 -> List.of("sse", "email");
            case 3 -> List.of("sse", "email", "wecom");
            case 4 -> List.of("sse", "email", "wecom", "dingtalk", "feishu");
            default -> List.of("sse");
        };
    }

    private List<String> resolveTargets(String channelName, AlertEvent event) {
        if (event.getReceiverIds() == null || event.getReceiverIds().isEmpty()) {
            return List.of();
        }

        return switch (channelName) {
            case "sse", "email" -> event.getReceiverIds().stream()
                    .map(String::valueOf)
                    .toList();
            case "wecom" -> properties.getChannels().getWecom().getWebhookUrl() != null
                    ? List.of(properties.getChannels().getWecom().getWebhookUrl())
                    : List.of();
            case "dingtalk" -> properties.getChannels().getDingtalk().getWebhookUrl() != null
                    ? List.of(properties.getChannels().getDingtalk().getWebhookUrl())
                    : List.of();
            case "feishu" -> properties.getChannels().getFeishu().getWebhookUrl() != null
                    ? List.of(properties.getChannels().getFeishu().getWebhookUrl())
                    : List.of();
            default -> List.of();
        };
    }
}