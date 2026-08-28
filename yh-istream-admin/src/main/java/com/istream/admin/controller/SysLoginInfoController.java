package com.istream.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.istream.common.annotation.OperLog;
import com.istream.common.enums.BusinessType;
import com.istream.common.model.R;
import com.istream.common.model.dto.SysLoginInfoQuery;
import com.istream.framework.util.ExcelExportUtil;
import com.istream.system.entity.SysLoginInfo;
import com.istream.system.service.SysLoginInfoService;
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

@Tag(name = "登录日志管理")
@RestController
@RequestMapping("/monitor/login-info")
@RequiredArgsConstructor
public class SysLoginInfoController {

    private final SysLoginInfoService sysLoginInfoService;

    @Operation(summary = "分页查询登录日志")
    @SaCheckPermission("system:login-info:list")
    @GetMapping("/list")
    public R<IPage<SysLoginInfo>> list(SysLoginInfoQuery query) {
        Page<SysLoginInfo> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<SysLoginInfo> wrapper = new LambdaQueryWrapper<SysLoginInfo>()
                .eq(query.getUsername() != null && !query.getUsername().isEmpty(),
                        SysLoginInfo::getUsername, query.getUsername())
                .eq(query.getIpAddress() != null && !query.getIpAddress().isEmpty(),
                        SysLoginInfo::getIpAddress, query.getIpAddress())
                .eq(query.getStatus() != null, SysLoginInfo::getStatus, query.getStatus())
                .orderByDesc(SysLoginInfo::getLoginTime);
        sysLoginInfoService.page(page, wrapper);
        return R.ok(page);
    }

    @Operation(summary = "根据ID查询登录日志")
    @SaCheckPermission("system:login-info:query")
    @GetMapping("/{id}")
    public R<SysLoginInfo> getById(@PathVariable Long id) {
        return R.ok(sysLoginInfoService.getById(id));
    }

    @OperLog(title = "登录日志管理", businessType = BusinessType.DELETE)
    @Operation(summary = "删除登录日志")
    @SaCheckPermission("system:login-info:delete")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        sysLoginInfoService.removeById(id);
        return R.ok();
    }

    @OperLog(title = "登录日志管理", businessType = BusinessType.DELETE)
    @Operation(summary = "批量删除登录日志")
    @SaCheckPermission("system:login-info:delete")
    @DeleteMapping("/batch")
    public R<Void> deleteBatch(@RequestBody List<Long> ids) {
        sysLoginInfoService.removeByIds(ids);
        return R.ok();
    }

    @OperLog(title = "登录日志管理", businessType = BusinessType.DELETE)
    @Operation(summary = "清空登录日志")
    @SaCheckPermission("system:login-info:clean")
    @DeleteMapping("/clear")
    public R<Void> clear() {
        sysLoginInfoService.truncate();
        return R.ok();
    }

    @Operation(summary = "导出登录日志")
    @SaCheckPermission("system:login-info:list")
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws IOException {
        List<SysLoginInfo> list = sysLoginInfoService.list(
                new LambdaQueryWrapper<SysLoginInfo>().orderByDesc(SysLoginInfo::getLoginTime));
        ExcelExportUtil.export(response, "登录日志", "登录日志", SysLoginInfo.class, list);
    }
}