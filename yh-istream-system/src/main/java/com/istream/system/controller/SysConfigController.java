package com.istream.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.istream.common.annotation.OperLog;
import com.istream.common.enums.BusinessType;
import com.istream.system.model.query.config.SysConfigQuery;
import com.istream.common.model.R;
import com.istream.common.enums.ResultCode;
import com.istream.common.validation.Groups;
import com.istream.system.model.dto.config.SysConfigSaveDTO;
import com.istream.system.model.dto.config.SysConfigDTO;
import com.istream.system.converter.SysConfigConverter;
import com.istream.system.entity.SysConfig;
import com.istream.system.service.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 系统配置管理控制器
 *
 * @author istream
 * @since 2026-08-17
 */
@Tag(name = "系统配置管理")
@RestController
@RequestMapping("/system/config")
@RequiredArgsConstructor
public class SysConfigController {

    private final SysConfigService sysConfigService;
    private final SysConfigConverter sysConfigConverter;

    @Operation(summary = "分页查询配置")
    @SaCheckPermission("system:config:list")
    @GetMapping("/list")
    public R<IPage<SysConfigDTO>> list(SysConfigQuery query) {
        IPage<SysConfig> page = sysConfigService.page(query);
        IPage<SysConfigDTO> dtoPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        dtoPage.setRecords(page.getRecords().stream()
                .map(sysConfigConverter::toDto)
                .toList());
        return R.ok(dtoPage);
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
    public R<SysConfigDTO> getById(@PathVariable Long id) {
        return R.ok(sysConfigConverter.toDto(sysConfigService.getById(id)));
    }

    @OperLog(title = "系统配置管理", businessType = BusinessType.INSERT)
    @Operation(summary = "新增配置")
    @SaCheckPermission("system:config:add")
    @PostMapping
    public R<Void> add(@Validated(Groups.Create.class) @RequestBody SysConfigSaveDTO dto) {
        if (sysConfigService.existsByConfigKey(dto.getConfigKey(), null)) {
            return R.fail(ResultCode.DATA_DUPLICATE, "配置键已存在");
        }
        SysConfig config = sysConfigConverter.toEntity(dto);
        sysConfigService.save(config);
        return R.ok();
    }

    @OperLog(title = "系统配置管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改配置")
    @SaCheckPermission("system:config:edit")
    @PutMapping
    public R<Void> update(@Validated(Groups.Update.class) @RequestBody SysConfigSaveDTO dto) {
        if (dto.getConfigKey() != null && sysConfigService.existsByConfigKey(dto.getConfigKey(), dto.getId())) {
            return R.fail(ResultCode.DATA_DUPLICATE, "配置键已存在");
        }
        SysConfig config = new SysConfig();
        config.setId(dto.getId());
        sysConfigConverter.updateEntity(config, dto);
        sysConfigService.updateById(config);
        return R.ok();
    }

    @OperLog(title = "系统配置管理", businessType = BusinessType.DELETE)
    @Operation(summary = "删除配置")
    @SaCheckPermission("system:config:delete")
    @DeleteMapping
    public R<Void> delete(@RequestBody List<Long> ids) {
        sysConfigService.removeByIds(ids);
        return R.ok();
    }
}