package com.istream.watchdog.aggregator;

import com.istream.watchdog.model.AlertEvent;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 告警聚合器
 *
 * <p>同一规则同一实体在聚合窗口内的多次告警合并为 1 条，
 * 避免短时间内重复推送相同告警（如批量更新触发多次）。</p>
 *
 * <h3>聚合策略</h3>
 * <p>Redis Key: {@code watchdog:aggregate:{ruleName}:{entityType}:{entityId}}</p>
 * <p>窗口内首次告警直接通过，后续告警更新已有记录但不重新推送。</p>
 *
 * @author istream
 * @since 2026-09-24
 */
@Slf4j
@Component
public class AlertAggregator {

    private static final String KEY_PREFIX = "watchdog:aggregate:";
    private static final Duration DEFAULT_WINDOW = Duration.ofMinutes(10);

    private final RedissonClient redissonClient;
    private final Duration window;

    public AlertAggregator(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
        this.window = DEFAULT_WINDOW;
    }

    /**
     * 检查告警是否应被聚合（抑制）
     *
     * @param event 告警事件
     * @return true 表示应推送（首次或窗口外），false 表示应抑制（窗口内重复）
     */
    public boolean shouldEmit(AlertEvent event) {
        String key = buildKey(event);
        RBucket<LocalDateTime> bucket = redissonClient.getBucket(key);

        LocalDateTime lastAlert = bucket.get();
        if (lastAlert != null && lastAlert.plus(window).isAfter(LocalDateTime.now())) {
            log.debug("AlertAggregator: 告警被聚合抑制 rule={}, entity={}/{}",
                    event.getRuleName(), event.getEntityType(), event.getEntityId());
            return false;
        }

        bucket.set(LocalDateTime.now(), window.toSeconds(), TimeUnit.SECONDS);
        return true;
    }

    private String buildKey(AlertEvent event) {
        return KEY_PREFIX + event.getRuleName() + ":" + event.getEntityType() + ":" + event.getEntityId();
    }
}