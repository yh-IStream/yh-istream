package com.istream.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.istream.common.annotation.OperLog;
import com.istream.common.enums.BusinessType;
import com.istream.common.enums.ResultCode;
import com.istream.common.model.R;
import com.istream.system.model.dto.SysDeptCreateDTO;
import com.istream.system.model.dto.SysDeptDTO;
import com.istream.system.model.dto.SysDeptUpdateDTO;
import com.istream.system.converter.SysDeptConverter;
import com.istream.system.entity.SysDept;
import com.istream.system.service.SysDeptService;
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
 * 部门管理控制器
 *
 * @author istream
 * @since 2026-08-17
 */
@Tag(name = "部门管理")
@RestController
@RequestMapping("/system/dept")
@RequiredArgsConstructor
public class SysDeptController {

    private final SysDeptService sysDeptService;
    private final SysDeptConverter sysDeptConverter;

    @Operation(summary = "查询部门树")
    @SaCheckPermission("system:dept:list")
    @GetMapping("/tree")
    public R<List<SysDeptDTO>> tree() {
        return R.ok(sysDeptService.listDeptTree().stream()
                .map(sysDeptConverter::toDto)
                .toList());
    }

    @Operation(summary = "根据ID查询部门")
    @SaCheckPermission("system:dept:query")
    @GetMapping("/{id}")
    public R<SysDeptDTO> getById(@PathVariable Long id) {
        return R.ok(sysDeptConverter.toDto(sysDeptService.getById(id)));
    }

    @OperLog(title = "部门管理", businessType = BusinessType.INSERT)
    @Operation(summary = "新增部门")
    @SaCheckPermission("system:dept:add")
    @PostMapping
    public R<Void> add(@Valid @RequestBody SysDeptCreateDTO dto) {
        SysDept dept = sysDeptConverter.toEntity(dto);
        sysDeptService.save(dept);
        return R.ok();
    }

    @OperLog(title = "部门管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改部门")
    @SaCheckPermission("system:dept:edit")
    @PutMapping
    public R<Void> update(@Valid @RequestBody SysDeptUpdateDTO dto) {
        if (dto.getId().equals(dto.getParentId())) {
            return R.fail(ResultCode.PARAM_VALID_ERROR, "上级部门不能是自己");
        }
        SysDept dept = new SysDept();
        dept.setId(dto.getId());
        sysDeptConverter.updateEntity(dept, dto);
        sysDeptService.updateById(dept);
        return R.ok();
    }

    @OperLog(title = "部门管理", businessType = BusinessType.DELETE)
    @Operation(summary = "删除部门")
    @SaCheckPermission("system:dept:delete")
    @DeleteMapping
    public R<Void> delete(@RequestBody List<Long> ids) {
        sysDeptService.removeByIds(ids);
        return R.ok();
    }
}