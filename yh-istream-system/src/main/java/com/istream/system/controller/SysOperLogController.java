package com.istream.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.istream.common.annotation.OperLog;
import com.istream.common.constant.Constants;
import com.istream.common.enums.BusinessType;
import com.istream.common.model.R;
import com.istream.system.model.dto.operlog.SysOperLogDTO;
import com.istream.system.model.query.operlog.SysOperLogQuery;
import com.istream.framework.util.ExcelExportUtil;
import com.istream.system.converter.SysOperLogConverter;
import com.istream.system.entity.SysOperLog;
import com.istream.system.service.SysOperLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    private final SysOperLogConverter sysOperLogConverter;

    @Operation(summary = "分页查询操作日志")
    @SaCheckPermission("monitor:oper-log:list")
    @GetMapping("/list")
    public R<IPage<SysOperLogDTO>> list(SysOperLogQuery query) {
        IPage<SysOperLog> page = sysOperLogService.page(query);
        IPage<SysOperLogDTO> dtoPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        dtoPage.setRecords(page.getRecords().stream()
                .map(sysOperLogConverter::toDto)
                .toList());
        return R.ok(dtoPage);
    }

    @Operation(summary = "根据ID查询操作日志")
    @SaCheckPermission("monitor:oper-log:query")
    @GetMapping("/{id}")
    public R<SysOperLogDTO> getById(@PathVariable Long id) {
        return R.ok(sysOperLogConverter.toDto(sysOperLogService.getById(id)));
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
                (pageNum) -> sysOperLogService.pageExport(pageNum, Constants.EXPORT_PAGE_SIZE));
    }
}