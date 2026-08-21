package com.istream.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.istream.common.annotation.OperLog;
import com.istream.common.enums.BusinessType;
import com.istream.common.model.R;
import com.istream.common.enums.ResultCode;
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

@Tag(name = "部门管理")
@RestController
@RequestMapping("/system/dept")
@RequiredArgsConstructor
public class SysDeptController {

    private final SysDeptService sysDeptService;

    @Operation(summary = "查询部门树")
    @SaCheckPermission("system:dept:list")
    @GetMapping("/tree")
    public R<List<SysDept>> tree() {
        return R.ok(sysDeptService.listDeptTree());
    }

    @Operation(summary = "根据ID查询部门")
    @SaCheckPermission("system:dept:query")
    @GetMapping("/{id}")
    public R<SysDept> getById(@PathVariable Long id) {
        return R.ok(sysDeptService.getById(id));
    }

    @OperLog(title = "部门管理", businessType = BusinessType.INSERT)
    @Operation(summary = "新增部门")
    @SaCheckPermission("system:dept:add")
    @PostMapping
    public R<Void> add(@Valid @RequestBody SysDept dept) {
        dept.setId(null);
        sysDeptService.save(dept);
        return R.ok();
    }

    @OperLog(title = "部门管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改部门")
    @SaCheckPermission("system:dept:edit")
    @PutMapping
    public R<Void> update(@Valid @RequestBody SysDept dept) {
        if (dept.getId().equals(dept.getParentId())) {
            return R.fail(ResultCode.PARAM_VALID_ERROR, "上级部门不能是自己");
        }
        sysDeptService.updateById(dept);
        return R.ok();
    }

    @OperLog(title = "部门管理", businessType = BusinessType.DELETE)
    @Operation(summary = "删除部门")
    @SaCheckPermission("system:dept:delete")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        if (sysDeptService.hasChildren(id)) {
            return R.fail(ResultCode.HAS_CHILDREN);
        }
        if (sysDeptService.hasUsers(id)) {
            return R.fail(ResultCode.HAS_USERS);
        }
        sysDeptService.removeById(id);
        return R.ok();
    }
}