package com.istream.common.enums;

/**
 * SSE 事件类型枚举
 *
 * <p>定义消息中心支持的事件类型，前端按类型路由到不同组件：</p>
 * <ul>
 *   <li>{@link #DATA_CHANGE} → useTable 自动刷新表格数据</li>
 *   <li>{@link #NOTIFICATION} → Bell 铃铛消息通知</li>
 *   <li>{@link #SYSTEM} → 系统公告栏</li>
 * </ul>
 *
 * @author istream
 * @since 2026-09-21
 */
public enum EventType {

    /** 数据变更事件：实体创建/更新/删除，触发前端表格自动刷新 */
    DATA_CHANGE,

    /** 通知事件：业务通知（如审批结果、工单指派），触发铃铛红点 */
    NOTIFICATION,

    /** 系统事件：系统公告、维护通知，触发系统栏 */
    SYSTEM
}