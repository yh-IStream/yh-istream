package com.istream.message.event;

import com.istream.common.enums.EventType;
import com.istream.common.enums.SyncEventType;
import com.istream.common.event.DataChangeEvent;
import com.istream.message.center.MessageCenter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 数据变更事件监听器
 *
 * <p>监听 {@link DataChangeEvent}，将数据变更路由到 {@link MessageCenter}，
 * 以 DATA_CHANGE 类型发送通知（SSE 即推 + MySQL 持久化）。</p>
 *
 * <p>异步执行，不阻塞主业务流。监听失败只 warn 不抛异常。</p>
 *
 * @author istream
 * @since 2026-09-24
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataChangeListener {

    private final MessageCenter messageCenter;

    /**
     * 处理数据变更事件
     *
     * @param event 数据变更事件
     */
    @Async("asyncExecutor")
    @EventListener
    public void onDataChange(DataChangeEvent event) {
        try {
            String title = buildTitle(event);
            String content = buildContent(event);

            if (event.operatorId() != null) {
                messageCenter.sendToUser(event.operatorId(), title, content,
                        EventType.DATA_CHANGE, event.entityType(), event.entityId());
            } else if (event.tenantId() != null) {
                messageCenter.sendToTenant(event.tenantId(), title, content,
                        EventType.DATA_CHANGE, event.entityType(), event.entityId());
            } else {
                messageCenter.broadcast(title, content, EventType.DATA_CHANGE);
            }

            log.debug("DataChangeListener: 已路由数据变更事件 entityType={}, entityId={}, eventType={}",
                    event.entityType(), event.entityId(), event.syncEventType());
        } catch (Exception e) {
            log.warn("DataChangeListener: 处理数据变更事件失败 entityType={}, entityId={}",
                    event.entityType(), event.entityId(), e);
        }
    }

    private String buildTitle(DataChangeEvent event) {
        String action = switch (event.syncEventType()) {
            case CREATE -> "新增";
            case UPDATE -> "修改";
            case DELETE -> "删除";
        };
        return action + event.entityType();
    }

    private String buildContent(DataChangeEvent event) {
        String action = switch (event.syncEventType()) {
            case CREATE -> "新增了";
            case UPDATE -> "修改了";
            case DELETE -> "删除了";
        };
        return event.entityType() + " " + action + " (ID: " + event.entityId() + ")";
    }
}