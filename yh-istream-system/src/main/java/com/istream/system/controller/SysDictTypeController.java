package com.istream.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.istream.common.annotation.OperLog;
import com.istream.common.enums.BusinessType;
import com.istream.system.model.query.dict.SysDictTypeQuery;
import com.istream.common.model.R;
import com.istream.common.enums.ResultCode;
import com.istream.common.validation.Groups;
import com.istream.system.model.dto.dict.SysDictTypeSaveDTO;
import com.istream.system.model.dto.dict.SysDictTypeDTO;
import com.istream.system.converter.SysDictTypeConverter;
import com.istream.system.entity.SysDictType;
import com.istream.system.service.SysDictTypeService;
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
 * 字典类型管理控制器
 *
 * @author istream
 * @since 2026-08-17
 */
@Tag(name = "字典类型管理")
@RestController
@RequestMapping("/system/dict-type")
@RequiredArgsConstructor
public class SysDictTypeController {

    private final SysDictTypeService sysDictTypeService;
    private final SysDictTypeConverter sysDictTypeConverter;

    @Operation(summary = "分页查询字典类型")
    @SaCheckPermission("system:dict:list")
    @GetMapping("/list")
    public R<IPage<SysDictTypeDTO>> list(SysDictTypeQuery query) {
        IPage<SysDictType> page = sysDictTypeService.page(query);
        IPage<SysDictTypeDTO> dtoPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        dtoPage.setRecords(page.getRecords().stream()
                .map(sysDictTypeConverter::toDto)
                .toList());
        return R.ok(dtoPage);
    }

    @Operation(summary = "根据ID查询字典类型")
    @SaCheckPermission("system:dict:query")
    @GetMapping("/{id}")
    public R<SysDictTypeDTO> getById(@PathVariable Long id) {
        return R.ok(sysDictTypeConverter.toDto(sysDictTypeService.getById(id)));
    }

    @OperLog(title = "字典类型管理", businessType = BusinessType.INSERT)
    @Operation(summary = "新增字典类型")
    @SaCheckPermission("system:dict:add")
    @PostMapping
    public R<Void> add(@Validated(Groups.Create.class) @RequestBody SysDictTypeSaveDTO dto) {
        if (sysDictTypeService.existsByDictType(dto.getDictType(), null)) {
            return R.fail(ResultCode.DATA_DUPLICATE, "字典类型已存在");
        }
        SysDictType dictType = sysDictTypeConverter.toEntity(dto);
        sysDictTypeService.save(dictType);
        return R.ok();
    }

    @OperLog(title = "字典类型管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改字典类型")
    @SaCheckPermission("system:dict:edit")
    @PutMapping
    public R<Void> update(@Validated(Groups.Update.class) @RequestBody SysDictTypeSaveDTO dto) {
        if (dto.getDictType() != null && sysDictTypeService.existsByDictType(dto.getDictType(), dto.getId())) {
            return R.fail(ResultCode.DATA_DUPLICATE, "字典类型已存在");
        }
        SysDictType dictType = new SysDictType();
        dictType.setId(dto.getId());
        sysDictTypeConverter.updateEntity(dictType, dto);
        sysDictTypeService.updateById(dictType);
        return R.ok();
    }

    @OperLog(title = "字典类型管理", businessType = BusinessType.DELETE)
    @Operation(summary = "删除字典类型")
    @SaCheckPermission("system:dict:delete")
    @DeleteMapping
    public R<Void> delete(@RequestBody List<Long> ids) {
        sysDictTypeService.removeByIds(ids);
        return R.ok();
    }
}