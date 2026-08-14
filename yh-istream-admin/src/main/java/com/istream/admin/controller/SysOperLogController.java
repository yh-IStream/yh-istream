package com.istream.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.istream.common.model.R;
import com.istream.system.entity.SysOperLog;
import com.istream.system.service.SysOperLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "操作日志管理")
@RestController
@RequestMapping("/system/oper-log")
@RequiredArgsConstructor
public class SysOperLogController {

    private final SysOperLogService sysOperLogService;

    @Operation(summary = "分页查询操作日志")
    @SaCheckPermission("system:oper-log:list")
    @GetMapping("/list")
    public R<IPage<SysOperLog>> list(@RequestParam(defaultValue = "1") Long pageNum,
                                     @RequestParam(defaultValue = "10") Long pageSize) {
        Page<SysOperLog> page = new Page<>(pageNum, pageSize);
        sysOperLogService.page(page, new LambdaQueryWrapper<SysOperLog>()
                .orderByDesc(SysOperLog::getOperTime));
        return R.ok(page);
    }

    @Operation(summary = "根据ID查询操作日志")
    @SaCheckPermission("system:oper-log:query")
    @GetMapping("/{id}")
    public R<SysOperLog> getById(@PathVariable Long id) {
        return R.ok(sysOperLogService.getById(id));
    }

    @Operation(summary = "删除操作日志")
    @SaCheckPermission("system:oper-log:delete")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        sysOperLogService.removeById(id);
        return R.ok();
    }

    @Operation(summary = "批量删除操作日志")
    @SaCheckPermission("system:oper-log:delete")
    @DeleteMapping("/batch")
    public R<Void> deleteBatch(@RequestBody List<Long> ids) {
        sysOperLogService.removeByIds(ids);
        return R.ok();
    }

    @Operation(summary = "清空操作日志")
    @SaCheckPermission("system:oper-log:clean")
    @DeleteMapping("/clear")
    public R<Void> clear() {
        sysOperLogService.remove(new LambdaQueryWrapper<SysOperLog>()
                .isNotNull(SysOperLog::getId));
        return R.ok();
    }
}