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
 * <p>按批次删除过期日志，避免单条大范围 DELETE 长时间锁表。</p>
 *
 * @author istream
 * @since 2026-08-17
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LogCleanupJob {

    private static final int RETENTION_DAYS = 90;
    private static final int BATCH_SIZE = 5000;

    private final SysOperLogService sysOperLogService;
    private final SysLoginInfoService sysLoginInfoService;

    @XxlJob("logCleanupJob")
    public void execute() {
        XxlJobHelper.log("开始清理过期日志，保留天数: {}", RETENTION_DAYS);
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(RETENTION_DAYS);

        long operTotal = cleanOperLog(cutoffDate);
        XxlJobHelper.log("操作日志清理完成: {}", operTotal);

        long loginTotal = cleanLoginInfo(cutoffDate);
        XxlJobHelper.log("登录日志清理完成: {}", loginTotal);

        XxlJobHelper.handleSuccess("日志清理任务完成");
        log.info("日志清理任务完成: operTotal={}, loginTotal={}", operTotal, loginTotal);
    }

    private long cleanOperLog(LocalDateTime cutoffDate) {
        long total = 0;
        while (true) {
            long before = sysOperLogService.count(new LambdaQueryWrapper<SysOperLog>()
                    .lt(SysOperLog::getOperTime, cutoffDate));
            if (before == 0) {
                break;
            }
            sysOperLogService.remove(new LambdaQueryWrapper<SysOperLog>()
                    .lt(SysOperLog::getOperTime, cutoffDate)
                    .last("LIMIT " + BATCH_SIZE));
            long after = sysOperLogService.count(new LambdaQueryWrapper<SysOperLog>()
                    .lt(SysOperLog::getOperTime, cutoffDate));
            total += before - after;
            if (after == 0) {
                break;
            }
        }
        return total;
    }

    private long cleanLoginInfo(LocalDateTime cutoffDate) {
        long total = 0;
        while (true) {
            long before = sysLoginInfoService.count(new LambdaQueryWrapper<SysLoginInfo>()
                    .lt(SysLoginInfo::getLoginTime, cutoffDate));
            if (before == 0) {
                break;
            }
            sysLoginInfoService.remove(new LambdaQueryWrapper<SysLoginInfo>()
                    .lt(SysLoginInfo::getLoginTime, cutoffDate)
                    .last("LIMIT " + BATCH_SIZE));
            long after = sysLoginInfoService.count(new LambdaQueryWrapper<SysLoginInfo>()
                    .lt(SysLoginInfo::getLoginTime, cutoffDate));
            total += before - after;
            if (after == 0) {
                break;
            }
        }
        return total;
    }
}