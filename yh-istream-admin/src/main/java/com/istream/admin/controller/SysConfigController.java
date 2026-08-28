package com.istream.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.istream.common.annotation.OperLog;
import com.istream.common.enums.BusinessType;
import com.istream.common.model.BaseQuery;
import com.istream.common.model.R;
import com.istream.common.enums.ResultCode;
import com.istream.system.entity.SysConfig;
import com.istream.system.service.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public R<IPage<SysConfig>> list(BaseQuery query) {
        Page<SysConfig> page = new Page<>(query.getPageNum(), query.getPageSize());
        sysConfigService.page(page, new LambdaQueryWrapper<SysConfig>()
                .orderByAsc(SysConfig::getId));
        return R.ok(page);
    }

    /**
     * 根据配置键查询配置值（公开接口，无需登录）
     * <p>用于前端获取系统级配置参数，如系统名称、Logo 等</p>
     */
    @Operation(summary = "根据配置键查询配置值")
    @GetMapping("/key/{configKey}")
    public R<String> getByKey(@PathVariable String configKey) {
        return R.ok(sysConfigService.getConfigValueByKey(configKey));
    }

    @Operation(summary = "根据ID查询配置")
    @SaCheckPermission("system:config:query")
    @GetMapping("/{id}")
    public R<SysConfig> getById(@PathVariable Long id) {
        return R.ok(sysConfigService.getById(id));
    }

    @OperLog(title = "系统配置管理", businessType = BusinessType.INSERT)
    @Operation(summary = "新增配置")
    @SaCheckPermission("system:config:add")
    @PostMapping
    public R<Void> add(@Valid @RequestBody SysConfig config) {
        if (sysConfigService.count(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, config.getConfigKey())) > 0) {
            return R.fail(ResultCode.DATA_DUPLICATE, "配置键已存在");
        }
        config.setId(null);
        sysConfigService.save(config);
        return R.ok();
    }

    @OperLog(title = "系统配置管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改配置")
    @SaCheckPermission("system:config:edit")
    @PutMapping
    public R<Void> update(@Valid @RequestBody SysConfig config) {
        if (sysConfigService.count(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, config.getConfigKey())
                .ne(SysConfig::getId, config.getId())) > 0) {
            return R.fail(ResultCode.DATA_DUPLICATE, "配置键已存在");
        }
        sysConfigService.updateById(config);
        return R.ok();
    }

    @OperLog(title = "系统配置管理", businessType = BusinessType.DELETE)
    @Operation(summary = "删除配置")
    @SaCheckPermission("system:config:delete")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        sysConfigService.removeById(id);
        return R.ok();
    }

    @OperLog(title = "系统配置管理", businessType = BusinessType.DELETE)
    @Operation(summary = "批量删除配置")
    @SaCheckPermission("system:config:delete")
    @DeleteMapping("/batch")
    public R<Void> deleteBatch(@RequestBody List<Long> ids) {
        sysConfigService.removeByIds(ids);
        return R.ok();
    }
}