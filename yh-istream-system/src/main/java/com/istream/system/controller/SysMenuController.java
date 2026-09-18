package com.istream.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.istream.common.annotation.OperLog;
import com.istream.common.enums.BusinessType;
import com.istream.common.model.R;
import com.istream.common.validation.Groups;
import com.istream.system.model.dto.menu.SysMenuSaveDTO;
import com.istream.system.model.dto.menu.SysMenuDTO;
import com.istream.system.converter.SysMenuConverter;
import com.istream.system.entity.SysMenu;
import com.istream.system.service.SysMenuService;
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
 * 菜单管理控制器
 *
 * @author istream
 * @since 2026-08-17
 */
@Tag(name = "菜单管理")
@RestController
@RequestMapping("/system/menu")
@RequiredArgsConstructor
public class SysMenuController {

    private final SysMenuService sysMenuService;
    private final SysMenuConverter sysMenuConverter;

    @Operation(summary = "查询菜单树（管理页面，含按钮）")
    @SaCheckPermission("system:menu:list")
    @GetMapping("/tree")
    public R<List<SysMenuDTO>> tree() {
        return R.ok(sysMenuService.listAllMenuTree().stream()
                .map(sysMenuConverter::toDto)
                .toList());
    }

    @Operation(summary = "查询当前用户菜单树（侧边栏用）")
    @GetMapping("/user-tree")
    public R<List<SysMenuDTO>> userTree() {
        return R.ok(sysMenuService.getCurrentUserMenuTree().stream()
                .map(sysMenuConverter::toDto)
                .toList());
    }

    @Operation(summary = "根据ID查询菜单")
    @SaCheckPermission("system:menu:query")
    @GetMapping("/{id}")
    public R<SysMenuDTO> getById(@PathVariable Long id) {
        return R.ok(sysMenuConverter.toDto(sysMenuService.getById(id)));
    }

    @OperLog(title = "菜单管理", businessType = BusinessType.INSERT)
    @Operation(summary = "新增菜单")
    @SaCheckPermission("system:menu:add")
    @PostMapping
    public R<Void> add(@Validated(Groups.Create.class) @RequestBody SysMenuSaveDTO dto) {
        SysMenu menu = sysMenuConverter.toEntity(dto);
        sysMenuService.save(menu);
        return R.ok();
    }

    @OperLog(title = "菜单管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改菜单")
    @SaCheckPermission("system:menu:edit")
    @PutMapping
    public R<Void> update(@Validated(Groups.Update.class) @RequestBody SysMenuSaveDTO dto) {
        SysMenu menu = new SysMenu();
        menu.setId(dto.getId());
        sysMenuConverter.updateEntity(menu, dto);
        sysMenuService.updateById(menu);
        return R.ok();
    }

    @OperLog(title = "菜单管理", businessType = BusinessType.DELETE)
    @Operation(summary = "删除菜单")
    @SaCheckPermission("system:menu:delete")
    @DeleteMapping
    public R<Void> delete(@RequestBody List<Long> ids) {
        sysMenuService.removeByIds(ids);
        return R.ok();
    }
}