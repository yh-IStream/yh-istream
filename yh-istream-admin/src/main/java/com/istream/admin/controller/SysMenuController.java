package com.istream.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.istream.common.model.R;
import com.istream.common.enums.ResultCode;
import com.istream.system.entity.SysMenu;
import com.istream.system.service.SysMenuService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "菜单管理")
@RestController
@RequestMapping("/system/menu")
@RequiredArgsConstructor
public class SysMenuController {

    private final SysMenuService sysMenuService;

    @Operation(summary = "查询菜单树（管理页面，含按钮）")
    @SaCheckPermission("system:menu:list")
    @GetMapping("/tree")
    public R<List<SysMenu>> tree() {
        return R.ok(sysMenuService.listAllMenuTree());
    }

    @Operation(summary = "根据ID查询菜单")
    @SaCheckPermission("system:menu:query")
    @GetMapping("/{id}")
    public R<SysMenu> getById(@PathVariable Long id) {
        return R.ok(sysMenuService.getById(id));
    }

    @Operation(summary = "新增菜单")
    @SaCheckPermission("system:menu:add")
    @PostMapping
    public R<Void> add(@RequestBody SysMenu menu) {
        menu.setId(null);
        sysMenuService.save(menu);
        return R.ok();
    }

    @Operation(summary = "修改菜单")
    @SaCheckPermission("system:menu:edit")
    @PutMapping
    public R<Void> update(@RequestBody SysMenu menu) {
        if (menu.getId().equals(menu.getParentId())) {
            return R.fail(ResultCode.PARAM_VALID_ERROR, "上级菜单不能是自己");
        }
        sysMenuService.updateById(menu);
        return R.ok();
    }

    @Operation(summary = "删除菜单")
    @SaCheckPermission("system:menu:delete")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        if (sysMenuService.hasChildren(id)) {
            return R.fail(ResultCode.HAS_CHILDREN);
        }
        sysMenuService.removeById(id);
        return R.ok();
    }
}