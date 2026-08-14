package com.istream.system.listener;

import com.istream.common.event.OperLogEvent;
import com.istream.system.entity.SysOperLog;
import com.istream.system.service.SysOperLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OperLogListener {

    private final SysOperLogService sysOperLogService;

    @Async
    @EventListener
    public void handleOperLog(OperLogEvent event) {
        SysOperLog logEntry = new SysOperLog();
        logEntry.setTitle(event.getTitle());
        logEntry.setBusinessType(event.getBusinessType());
        logEntry.setMethod(event.getMethod());
        logEntry.setRequestMethod(event.getRequestMethod());
        logEntry.setOperUrl(event.getOperUrl());
        logEntry.setOperIp(event.getOperIp());
        logEntry.setOperParam(event.getOperParam());
        logEntry.setJsonResult(event.getJsonResult());
        logEntry.setStatus(event.getStatus());
        logEntry.setErrorMsg(event.getErrorMsg());
        logEntry.setCostTime(event.getCostTime());
        logEntry.setOperBy(event.getOperBy());
        logEntry.setOperName(event.getOperName());
        logEntry.setOperTime(event.getOperTime());
        sysOperLogService.save(logEntry);
    }
}