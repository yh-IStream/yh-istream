package com.istream.message.connection;

/**
 * SSE 连接注册表
 *
 * <p>维护用户与 SSE 连接的映射关系，支持单实例（内存）和多实例（Redis）两种模式。</p>
 * <p>单实例模式直接委托给 {@link com.istream.framework.sse.SseService} 的本地 emitters，
 * 多实例模式额外维护 Redis Hash 用于跨节点广播时定位连接所在节点。</p>
 *
 * @author istream
 * @since 2026-09-21
 */
public interface ConnectionRegistry {

    /**
     * 绑定用户连接
     *
     * @param userId 用户ID
     */
    void bind(Long userId);

    /**
     * 解绑用户连接
     *
     * @param userId 用户ID
     */
    void unbind(Long userId);

    /**
     * 获取当前在线用户数
     *
     * @return 在线用户数
     */
    int getConnectionCount();

    /**
     * 判断用户是否在线
     *
     * @param userId 用户ID
     * @return true=在线，false=离线
     */
    boolean isOnline(Long userId);
}