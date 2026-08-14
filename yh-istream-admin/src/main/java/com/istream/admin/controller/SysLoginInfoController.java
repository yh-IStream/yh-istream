package com.istream.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.istream.common.model.R;
import com.istream.system.entity.SysLoginInfo;
import com.istream.system.service.SysLoginInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "登录日志管理")
@RestController
@RequestMapping("/system/login-info")
@RequiredArgsConstructor
public class SysLoginInfoController {

    private final SysLoginInfoService sysLoginInfoService;

    @Operation(summary = "分页查询登录日志")
    @SaCheckPermission("system:login-info:list")
    @GetMapping("/list")
    public R<IPage<SysLoginInfo>> list(@RequestParam(defaultValue = "1") Long pageNum,
                                       @RequestParam(defaultValue = "10") Long pageSize) {
        Page<SysLoginInfo> page = new Page<>(pageNum, pageSize);
        sysLoginInfoService.page(page, new LambdaQueryWrapper<SysLoginInfo>()
                .orderByDesc(SysLoginInfo::getLoginTime));
        return R.ok(page);
    }

    @Operation(summary = "根据ID查询登录日志")
    @SaCheckPermission("system:login-info:query")
    @GetMapping("/{id}")
    public R<SysLoginInfo> getById(@PathVariable Long id) {
        return R.ok(sysLoginInfoService.getById(id));
    }

    @Operation(summary = "删除登录日志")
    @SaCheckPermission("system:login-info:delete")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        sysLoginInfoService.removeById(id);
        return R.ok();
    }

    @Operation(summary = "批量删除登录日志")
    @SaCheckPermission("system:login-info:delete")
    @DeleteMapping("/batch")
    public R<Void> deleteBatch(@RequestBody List<Long> ids) {
        sysLoginInfoService.removeByIds(ids);
        return R.ok();
    }

    @Operation(summary = "清空登录日志")
    @SaCheckPermission("system:login-info:clean")
    @DeleteMapping("/clear")
    public R<Void> clear() {
        sysLoginInfoService.remove(new LambdaQueryWrapper<SysLoginInfo>()
                .isNotNull(SysLoginInfo::getId));
        return R.ok();
    }
}