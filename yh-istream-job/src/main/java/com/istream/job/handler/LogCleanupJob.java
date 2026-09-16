
package com.istream.job.handler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.istream.system.entity.SysLoginInfo;
import com.istream.system.entity.SysOperLog;
import com.istream.system.service.SysLoginInfoService;
import com.istream.system.service.SysOperLogService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 日志清理任务
 *
 * @author istream
 * @since 2026-08-17
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LogCleanupJob {

    private static final int RETENTION_DAYS = 90;

    private final SysOperLogService sysOperLogService;
    private final SysLoginInfoService sysLoginInfoService;

    @XxlJob("logCleanupJob")
    public void execute() {
        XxlJobHelper.log("开始清理过期日志，保留天数: {}", RETENTION_DAYS);
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(RETENTION_DAYS);

        boolean operResult = sysOperLogService.remove(
                new LambdaQueryWrapper<SysOperLog>()
                        .lt(SysOperLog::getOperTime, cutoffDate));
        XxlJobHelper.log("操作日志清理完成: {}", operResult);

        boolean loginResult = sysLoginInfoService.remove(
                new LambdaQueryWrapper<SysLoginInfo>()
                        .lt(SysLoginInfo::getLoginTime, cutoffDate));
        XxlJobHelper.log("登录日志清理完成: {}", loginResult);

        XxlJobHelper.handleSuccess("日志清理任务完成");
        log.info("日志清理任务完成: operResult={}, loginResult={}", operResult, loginResult);
    }
}