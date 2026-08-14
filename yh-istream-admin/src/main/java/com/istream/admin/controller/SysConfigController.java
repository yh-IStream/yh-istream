package com.istream.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.istream.common.model.R;
import com.istream.common.enums.ResultCode;
import com.istream.system.entity.SysConfig;
import com.istream.system.service.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "系统配置管理")
@RestController
@RequestMapping("/system/config")
@RequiredArgsConstructor
public class SysConfigController {

    private final SysConfigService sysConfigService;

    @Operation(summary = "分页查询配置")
    @SaCheckPermission("system:config:list")
    @GetMapping("/list")
    public R<IPage<SysConfig>> list(@RequestParam(defaultValue = "1") Long pageNum,
                                    @RequestParam(defaultValue = "10") Long pageSize) {
        Page<SysConfig> page = new Page<>(pageNum, pageSize);
        sysConfigService.page(page, new LambdaQueryWrapper<SysConfig>()
                .orderByAsc(SysConfig::getId));
        return R.ok(page);
    }

    @Operation(summary = "根据配置键查询配置值")
    @GetMapping("/key/{configKey}")
    public R<String> getByKey(@PathVariable String configKey) {
        SysConfig config = sysConfigService.getOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, configKey));
        return R.ok(config != null ? config.getConfigValue() : null);
    }

    @Operation(summary = "根据ID查询配置")
    @SaCheckPermission("system:config:query")
    @GetMapping("/{id}")
    public R<SysConfig> getById(@PathVariable Long id) {
        return R.ok(sysConfigService.getById(id));
    }

    @Operation(summary = "新增配置")
    @SaCheckPermission("system:config:add")
    @PostMapping
    public R<Void> add(@RequestBody SysConfig config) {
        if (sysConfigService.count(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, config.getConfigKey())) > 0) {
            return R.fail(ResultCode.DATA_DUPLICATE, "配置键已存在");
        }
        config.setId(null);
        sysConfigService.save(config);
        return R.ok();
    }

    @Operation(summary = "修改配置")
    @SaCheckPermission("system:config:edit")
    @PutMapping
    public R<Void> update(@RequestBody SysConfig config) {
        sysConfigService.updateById(config);
        return R.ok();
    }

    @Operation(summary = "删除配置")
    @SaCheckPermission("system:config:delete")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        sysConfigService.removeById(id);
        return R.ok();
    }

    @Operation(summary = "批量删除配置")
    @SaCheckPermission("system:config:delete")
    @DeleteMapping("/batch")
    public R<Void> deleteBatch(@RequestBody List<Long> ids) {
        sysConfigService.removeByIds(ids);
        return R.ok();
    }
}