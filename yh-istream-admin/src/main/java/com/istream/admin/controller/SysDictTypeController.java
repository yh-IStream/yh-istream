package com.istream.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.istream.common.annotation.OperLog;
import com.istream.common.enums.BusinessType;
import com.istream.common.model.query.SysDictTypeQuery;
import com.istream.common.model.R;
import com.istream.common.enums.ResultCode;
import com.istream.system.entity.SysDictType;
import com.istream.system.service.SysDictTypeService;
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

    @Operation(summary = "分页查询字典类型")
    @SaCheckPermission("system:dict:list")
    @GetMapping("/list")
    public R<IPage<SysDictType>> list(SysDictTypeQuery query) {
        Page<SysDictType> page = new Page<>(query.getPageNum(), query.getPageSize());
        sysDictTypeService.page(page, new LambdaQueryWrapper<SysDictType>()
                .like(query.getDictName() != null && !query.getDictName().isEmpty(),
                        SysDictType::getDictName, query.getDictName())
                .like(query.getDictType() != null && !query.getDictType().isEmpty(),
                        SysDictType::getDictType, query.getDictType())
                .orderByDesc(SysDictType::getCreateTime));
        return R.ok(page);
    }

    @Operation(summary = "根据ID查询字典类型")
    @SaCheckPermission("system:dict:query")
    @GetMapping("/{id}")
    public R<SysDictType> getById(@PathVariable Long id) {
        return R.ok(sysDictTypeService.getById(id));
    }

    @OperLog(title = "字典类型管理", businessType = BusinessType.INSERT)
    @Operation(summary = "新增字典类型")
    @SaCheckPermission("system:dict:add")
    @PostMapping
    public R<Void> add(@Valid @RequestBody SysDictType dictType) {
        if (sysDictTypeService.count(new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getDictType, dictType.getDictType())) > 0) {
            return R.fail(ResultCode.DATA_DUPLICATE, "字典类型已存在");
        }
        dictType.setId(null);
        sysDictTypeService.save(dictType);
        return R.ok();
    }

    @OperLog(title = "字典类型管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改字典类型")
    @SaCheckPermission("system:dict:edit")
    @PutMapping
    public R<Void> update(@Valid @RequestBody SysDictType dictType) {
        SysDictType exist = sysDictTypeService.getOne(new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getDictType, dictType.getDictType()));
        if (exist != null && !exist.getId().equals(dictType.getId())) {
            return R.fail(ResultCode.DATA_DUPLICATE, "字典类型已存在");
        }
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