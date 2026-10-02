package com.istream.system.listener;

import com.istream.common.event.OperLogEvent;
import com.istream.common.model.sse.SseEvent;
import com.istream.framework.sse.SseService;
import com.istream.system.entity.SysOperLog;
import com.istream.system.service.SysOperLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 操作日志事件监听器
 *
 * <p>异步消费 {@link OperLogEvent}，将操作日志持久化到数据库并广播 SSE 事件。
 * 内部增加异常保护，确保日志写入失败不影响主流程和 SSE 推送。</p>
 *
 * @author istream
 * @since 2026-08-17
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OperLogListener {

    private final SysOperLogService sysOperLogService;
    private final SseService sseService;

    /**
     * 处理操作日志事件
     *
     * @param event 操作日志事件
     */
    @Async("asyncExecutor")
    @EventListener
    public void handleOperLog(OperLogEvent event) {
        SysOperLog logEntry = convertToEntity(event);

        try {
            sysOperLogService.save(logEntry);
        } catch (Exception e) {
            log.error("操作日志持久化失败: title={}, method={}", event.title(), event.method(), e);
        }

        try {
            sseService.broadcast(SseEvent.of("OPER_LOG", logEntry));
        } catch (Exception e) {
            log.error("操作日志SSE广播失败: title={}", event.title(), e);
        }
    }

    /**
     * 将事件对象转换为持久化实体
     */
    private SysOperLog convertToEntity(OperLogEvent event) {
        SysOperLog logEntry = new SysOperLog();
        logEntry.setTitle(event.title());
        logEntry.setBusinessType(event.businessType());
        logEntry.setMethod(event.method());
        logEntry.setRequestMethod(event.requestMethod());
        logEntry.setOperUrl(event.operUrl());
        logEntry.setOperIp(event.operIp());
        logEntry.setOperLocation(event.operLocation());
        logEntry.setOperParam(event.operParam());
        logEntry.setJsonResult(event.jsonResult());
        logEntry.setStatus(event.status());
        logEntry.setErrorMsg(event.errorMsg());
        logEntry.setCostTime(event.costTime());
        logEntry.setOperBy(event.operBy());
        logEntry.setOperName(event.operName());
        logEntry.setOperTime(event.operTime());
        return logEntry;
    }
}