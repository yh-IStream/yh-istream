package com.istream.message.connection;

import com.istream.framework.sse.SseConnectionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 支持 Redis 的 SSE 连接注册表
 *
 * <p>本地维护 ConcurrentHashMap 用于本节点 SSE 推送，
 * Redis Hash 维护 userId → nodeId 映射用于多实例广播时定位连接所在节点。</p>
 * <p>通过 {@link EventListener} 监听 {@link SseConnectionEvent} 自动同步连接状态，
 * Redisson 不可用时降级为纯本地内存模式（单实例部署场景）。</p>
 *
 * @author istream
 * @since 2026-09-21
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisConnectionRegistry implements ConnectionRegistry {

    private static final String REDIS_KEY = "sse:connections";

    private final Map<Long, String> localConnections = new ConcurrentHashMap<>();

    private final RedissonClient redissonClient;

    /**
     * 监听 SSE 连接建立事件
     */
    @EventListener
    public void onConnected(SseConnectionEvent event) {
        if (event.getEventType() == SseConnectionEvent.Type.CONNECTED) {
            bind(event.getUserId());
        }
    }

    /**
     * 监听 SSE 连接断开事件
     */
    @EventListener
    public void onDisconnected(SseConnectionEvent event) {
        if (event.getEventType() == SseConnectionEvent.Type.DISCONNECTED) {
            unbind(event.getUserId());
        }
    }

    @Override
    public void bind(Long userId) {
        String nodeId = getNodeId();
        localConnections.put(userId, nodeId);
        try {
            redissonClient.getMapCache(REDIS_KEY).put(userId, nodeId);
        } catch (Exception e) {
            log.debug("Redis 连接注册失败（降级为本地模式）: userId={}", userId, e);
        }
        log.debug("SSE 连接绑定: userId={}, nodeId={}", userId, nodeId);
    }

    @Override
    public void unbind(Long userId) {
        localConnections.remove(userId);
        try {
            redissonClient.getMapCache(REDIS_KEY).remove(userId);
        } catch (Exception e) {
            log.debug("Redis 连接注销失败（降级为本地模式）: userId={}", userId, e);
        }
        log.debug("SSE 连接解绑: userId={}", userId);
    }

    @Override
    public int getConnectionCount() {
        return localConnections.size();
    }

    @Override
    public boolean isOnline(Long userId) {
        return localConnections.containsKey(userId);
    }

    /**
     * 判断用户是否在本节点在线
     *
     * @param userId 用户ID
     * @return true=本节点在线
     */
    public boolean isLocalOnline(Long userId) {
        return localConnections.containsKey(userId);
    }

    /**
     * 获取当前节点标识
     */
    private String getNodeId() {
        return System.getProperty("node.id", "default");
    }
}