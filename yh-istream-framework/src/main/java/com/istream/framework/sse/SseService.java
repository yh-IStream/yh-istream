package com.istream.framework.sse;

import com.istream.common.enums.ResultCode;
import com.istream.common.exception.BusinessException;
import com.istream.common.model.sse.SseEvent;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Duration;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <h3>SSE 实时推送服务</h3>
 * <p>管理客户端 SSE 连接，支持广播和单播。每30秒发送心跳保活，
 * 连接超时或异常时自动清理。连接建立/断开时发布 {@link SseConnectionEvent} 事件。
 * 心跳在虚拟线程上并发发送，避免写入阻塞导致拖住其他用户。同一用户维护一个连接集合，支持
 * 多标签页、多端同时订阅。仅当该用户的最后一条连接断开时发布 {@link SseConnectionEvent.Type#DISCONNECTED}，
 * 避免误判。</p>
 *
 * @author istream
 * @since 2026-08-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SseService {

    private static final long SSE_TIMEOUT = 300_000L;
    private static final long HEARTBEAT_INTERVAL_MS = 30_000L;
    private static final String TICKET_PREFIX = "sse:ticket:";
    private static final long TICKET_TTL_SECONDS = 30L;
    private static final int MAX_CONCURRENT_SEND = 64;
    private static final long SHUTDOWN_AWAIT_SECONDS = 5L;

    /**
     * 单用户连接数上限
     */
    private static final int MAX_CONNECTIONS_PER_USER = 8;

    private final ConcurrentHashMap<Long, Set<SseEmitter>> emitters = new ConcurrentHashMap<>();

    private final ExecutorService heartbeatExecutor = Executors.newVirtualThreadPerTaskExecutor();

    /**
     * 心跳并发发送许可，以免连接数激增时无限创建虚拟线程
     */
    private final Semaphore sendPermits = new Semaphore(MAX_CONCURRENT_SEND);

    private final ApplicationEventPublisher eventPublisher;
    private final RedissonClient redissonClient;

    /**
     * 客户端订阅 SSE 连接
     *
     * <p>同一用户可建立多条连接（多标签页/多端），不再踢掉旧连接。</p>
     *
     * @param userId 用户ID
     * @return SseEmitter 实例
     */
    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        if (!attach(userId, emitter)) {
            log.warn("SSE 连接数超出单用户上限，拒绝新连接: userId={}, limit={}",
                    userId, MAX_CONNECTIONS_PER_USER);
            emitter.completeWithError(new BusinessException(
                    ResultCode.RATE_LIMIT, "SSE 连接数超出上限，请关闭多余的页面后重试"));
            return emitter;
        }

        emitter.onCompletion(() -> detach(userId, emitter));
        emitter.onTimeout(() -> detach(userId, emitter));
        emitter.onError(ex -> detach(userId, emitter));

        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data(SseEvent.of("CONNECTED", "SSE 连接已建立")));
        } catch (Exception e) {
            log.warn("SSE 初始握手失败: userId={}", userId, e);
            detach(userId, emitter);
            return emitter;
        }

        log.info("SSE 客户端已连接: userId={}, 连接数={}, 在线用户数={}",
                userId, getConnectionCount(), getOnlineUserCount());
        eventPublisher.publishEvent(new SseConnectionEvent(SseConnectionEvent.Type.CONNECTED, userId));
        return emitter;
    }

    /**
     * 广播事件给所有已连接客户端
     *
     * @param event SSE 事件
     */
    public void broadcast(SseEvent event) {
        if (emitters.isEmpty()) {
            return;
        }
        log.debug("SSE 广播: type={}, 在线用户数={}", event.type(), emitters.size());
        emitters.forEach((userId, connections) ->
                connections.forEach(emitter -> sendEvent(userId, emitter, event)));
    }

    /**
     * 向指定用户的所有连接发送事件
     *
     * @param userId 用户ID
     * @param event  SSE 事件
     */
    public void sendToUser(Long userId, SseEvent event) {
        Set<SseEmitter> connections = emitters.get(userId);
        if (connections == null || connections.isEmpty()) {
            return;
        }
        connections.forEach(emitter -> sendEvent(userId, emitter, event));
    }

    /**
     * <p>每30秒发送心跳，心跳在虚拟线程上并发发送，单个客户端的写阻塞不会拖住其他用户。</p>
     */
    @Scheduled(fixedRate = HEARTBEAT_INTERVAL_MS)
    public void heartbeat() {
        if (emitters.isEmpty()) {
            return;
        }
        SseEvent heartbeat = SseEvent.heartbeat();
        emitters.forEach((userId, connections) ->
                connections.forEach(emitter -> dispatchHeartbeat(userId, emitter, heartbeat)));
    }

    /**
     * 获取当前连接总数（含同一用户的多条连接）
     *
     * @return 连接数
     */
    public int getConnectionCount() {
        return emitters.values().stream().mapToInt(Set::size).sum();
    }

    /**
     * 获取当前在线用户数
     *
     * @return 在线用户数
     */
    public int getOnlineUserCount() {
        return emitters.size();
    }

    /**
     * 生成一次性 SSE 连接凭证
     * <p>凭证 30 秒有效，使用后即焚（一次性），避免主 Token 暴露在 URL 中。</p>
     *
     * @param userId 用户ID
     * @return ticket 字符串
     */
    public String generateTicket(Long userId) {
        String ticket = UUID.randomUUID().toString().replace("-", "");
        RBucket<Long> bucket = redissonClient.getBucket(TICKET_PREFIX + ticket);
        bucket.set(userId, Duration.ofSeconds(TICKET_TTL_SECONDS));
        return ticket;
    }

    /**
     * 验证并消费一次性 ticket
     * <p>ticket 验证后立即删除，确保一次性使用。</p>
     *
     * @param ticket 凭证字符串
     * @return 用户ID，若 ticket 无效或已过期返回 null
     */
    public Long consumeTicket(String ticket) {
        if (ticket == null || ticket.isBlank()) {
            return null;
        }
        RBucket<Long> bucket = redissonClient.getBucket(TICKET_PREFIX + ticket);
        return bucket.getAndDelete();
    }

    /**
     * 优雅停机时关闭心跳执行器
     */
    @PreDestroy
    public void shutdown() {
        heartbeatExecutor.shutdown();
        try {
            if (!heartbeatExecutor.awaitTermination(SHUTDOWN_AWAIT_SECONDS, TimeUnit.SECONDS)) {
                heartbeatExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            heartbeatExecutor.shutdownNow();
        }
        log.info("SSE 心跳执行器已关闭");
    }

    /**
     * 将连接加入用户的连接集合
     */
    private boolean attach(Long userId, SseEmitter emitter) {
        AtomicBoolean accepted = new AtomicBoolean(false);
        emitters.compute(userId, (key, connections) -> {
            if (connections == null) {
                Set<SseEmitter> created = ConcurrentHashMap.newKeySet();
                created.add(emitter);
                accepted.set(true);
                return created;
            }
            if (connections.size() >= MAX_CONNECTIONS_PER_USER) {
                return connections;
            }
            connections.add(emitter);
            accepted.set(true);
            return connections;
        });
        return accepted.get();
    }

    /**
     * 从用户的连接集合移除连接
     *
     * <p>集合为空时删除用户条目，并发布 {@link SseConnectionEvent.Type#DISCONNECTED}</p>
     */
    private void detach(Long userId, SseEmitter emitter) {
        AtomicBoolean lastConnection = new AtomicBoolean(false);
        emitters.computeIfPresent(userId, (key, connections) -> {
            connections.remove(emitter);
            if (connections.isEmpty()) {
                lastConnection.set(true);
                return null;
            }
            return connections;
        });
        if (lastConnection.get()) {
            log.debug("SSE 用户最后一条连接已断开: userId={}", userId);
            eventPublisher.publishEvent(new SseConnectionEvent(SseConnectionEvent.Type.DISCONNECTED, userId));
        }
    }

    /**
     * 将心跳提交到虚拟线程执行
     *
     * <p>许可不足时跳过本轮</p>
     */
    private void dispatchHeartbeat(Long userId, SseEmitter emitter, SseEvent heartbeat) {
        if (!sendPermits.tryAcquire()) {
            log.debug("SSE 心跳并发已达上限，跳过本轮: userId={}", userId);
            return;
        }
        try {
            heartbeatExecutor.execute(() -> {
                try {
                    sendEvent(userId, emitter, heartbeat);
                } finally {
                    sendPermits.release();
                }
            });
        } catch (RejectedExecutionException e) {
            sendPermits.release();
            log.debug("SSE 心跳执行器已关闭，跳过发送: userId={}", userId);
        }
    }

    /**
     * 向单个连接发送事件，失败时移除连接
     */
    private void sendEvent(Long userId, SseEmitter emitter, SseEvent event) {
        try {
            emitter.send(SseEmitter.event()
                    .name(event.type().toLowerCase(Locale.ROOT))
                    .data(event));
        } catch (Exception e) {
            log.debug("SSE 发送失败，移除连接: userId={}, type={}", userId, event.type());
            detach(userId, emitter);
            try {
                emitter.completeWithError(e);
            } catch (Exception closeError) {
                log.debug("SSE 连接关闭失败（可能已结束）: userId={}", userId, closeError);
            }
        }
    }
}