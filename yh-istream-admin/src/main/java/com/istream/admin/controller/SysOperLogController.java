package com.istream.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.istream.common.annotation.OperLog;
import com.istream.common.enums.BusinessType;
import com.istream.common.model.R;
import com.istream.common.model.query.SysOperLogQuery;
import com.istream.framework.util.ExcelExportUtil;
import com.istream.system.entity.SysOperLog;
import com.istream.system.service.SysOperLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * 操作日志管理控制器
 *
 * @author istream
 * @since 2026-08-17
 */
@Tag(name = "操作日志管理")
@RestController
@RequestMapping("/monitor/oper-log")
@RequiredArgsConstructor
public class SysOperLogController {

    private final SysOperLogService sysOperLogService;

    @Operation(summary = "分页查询操作日志")
    @SaCheckPermission("monitor:oper-log:list")
    @GetMapping("/list")
    public R<IPage<SysOperLog>> list(SysOperLogQuery query) {
        Page<SysOperLog> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<SysOperLog> wrapper = new LambdaQueryWrapper<SysOperLog>()
                .like(query.getTitle() != null && !query.getTitle().isEmpty(),
                        SysOperLog::getTitle, query.getTitle())
                .eq(query.getBusinessType() != null, SysOperLog::getBusinessType, query.getBusinessType())
                .eq(query.getStatus() != null, SysOperLog::getStatus, query.getStatus())
                .orderByDesc(SysOperLog::getOperTime);
        sysOperLogService.page(page, wrapper);
        return R.ok(page);
    }

    @Operation(summary = "根据ID查询操作日志")
    @SaCheckPermission("monitor:oper-log:query")
    @GetMapping("/{id}")
    public R<SysOperLog> getById(@PathVariable Long id) {
        return R.ok(sysOperLogService.getById(id));
    }

    @OperLog(title = "操作日志管理", businessType = BusinessType.DELETE)
    @Operation(summary = "删除操作日志")
    @SaCheckPermission("monitor:oper-log:delete")
    @DeleteMapping
    public R<Void> delete(@RequestBody List<Long> ids) {
        sysOperLogService.removeByIds(ids);
        return R.ok();
    }

    @OperLog(title = "操作日志管理", businessType = BusinessType.DELETE)
    @Operation(summary = "清空操作日志")
    @SaCheckPermission("monitor:oper-log:clean")
    @DeleteMapping("/clear")
    public R<Void> clear() {
        sysOperLogService.truncate();
        return R.ok();
    }

    @Operation(summary = "导出操作日志")
    @SaCheckPermission("monitor:oper-log:export")
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws IOException {
        ExcelExportUtil.exportByPage(response, "操作日志", "操作日志", SysOperLog.class,
                (pageNum) -> {
                    Page<SysOperLog> page = new Page<>(pageNum, 5000);
                    sysOperLogService.page(page, new LambdaQueryWrapper<SysOperLog>()
                            .orderByDesc(SysOperLog::getOperTime));
                    return page;
                });
    }
}