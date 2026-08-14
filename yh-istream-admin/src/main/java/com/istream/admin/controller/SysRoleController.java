package com.istream.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.istream.common.model.dto.SysRoleQuery;
import com.istream.common.model.R;
import com.istream.common.enums.ResultCode;
import com.istream.system.entity.SysRole;
import com.istream.system.entity.SysMenu;
import com.istream.system.entity.SysUser;
import com.istream.system.service.SysMenuService;
import com.istream.system.service.SysRoleService;
import com.istream.system.service.SysUserService;
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

@Tag(name = "角色管理")
@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService sysRoleService;
    private final SysMenuService sysMenuService;
    private final SysUserService sysUserService;

    @Operation(summary = "分页查询角色列表")
    @SaCheckPermission("system:role:list")
    @GetMapping("/list")
    public R<IPage<SysRole>> list(SysRoleQuery query) {
        return R.ok(sysRoleService.page(query));
    }

    @Operation(summary = "查询所有角色（下拉选择用）")
    @SaCheckPermission("system:role:list")
    @GetMapping("/all")
    public R<List<SysRole>> all() {
        return R.ok(sysRoleService.list(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getStatus, 0)
                .eq(SysRole::getDelFlag, 0)
                .orderByAsc(SysRole::getRoleSort)));
    }

    @Operation(summary = "根据ID查询角色")
    @SaCheckPermission("system:role:query")
    @GetMapping("/{id}")
    public R<SysRole> getById(@PathVariable Long id) {
        return R.ok(sysRoleService.getById(id));
    }

    @Operation(summary = "新增角色")
    @SaCheckPermission("system:role:add")
    @PostMapping
    public R<Void> add(@RequestBody SysRole role) {
        if (sysRoleService.count(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleKey, role.getRoleKey())) > 0) {
            return R.fail(ResultCode.DATA_DUPLICATE, "角色标识已存在");
        }
        role.setId(null);
        sysRoleService.save(role);
        return R.ok();
    }

    @Operation(summary = "修改角色")
    @SaCheckPermission("system:role:edit")
    @PutMapping
    public R<Void> update(@RequestBody SysRole role) {
        SysRole exist = sysRoleService.getOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleKey, role.getRoleKey()));
        if (exist != null && !exist.getId().equals(role.getId())) {
            return R.fail(ResultCode.DATA_DUPLICATE, "角色标识已存在");
        }
        sysRoleService.updateById(role);
        return R.ok();
    }

    @Operation(summary = "删除角色")
    @SaCheckPermission("system:role:delete")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        sysRoleService.removeById(id);
        return R.ok();
    }

    @Operation(summary = "批量删除角色")
    @SaCheckPermission("system:role:delete")
    @DeleteMapping("/batch")
    public R<Void> deleteBatch(@RequestBody List<Long> ids) {
        sysRoleService.removeByIds(ids);
        return R.ok();
    }

    @Operation(summary = "修改角色状态")
    @SaCheckPermission("system:role:edit")
    @PutMapping("/change-status")
    public R<Void> changeStatus(@RequestBody SysRole role) {
        sysRoleService.changeStatus(role.getId(), role.getStatus());
        return R.ok();
    }

    @Operation(summary = "查询角色已分配的菜单ID列表")
    @SaCheckPermission("system:role:edit")
    @GetMapping("/{roleId}/menu-ids")
    public R<List<Long>> getMenuIds(@PathVariable Long roleId) {
        return R.ok(sysRoleService.getMenuIdsByRoleId(roleId));
    }

    @Operation(summary = "保存角色菜单分配")
    @SaCheckPermission("system:role:edit")
    @PutMapping("/{roleId}/menu")
    public R<Void> saveRoleMenu(@PathVariable Long roleId, @RequestBody List<Long> menuIds) {
        sysRoleService.saveRoleMenu(roleId, menuIds);
        return R.ok();
    }

    @Operation(summary = "查询角色已分配的用户列表")
    @SaCheckPermission("system:role:edit")
    @GetMapping("/{roleId}/users")
    public R<List<SysUser>> getUsers(@PathVariable Long roleId) {
        return R.ok(sysUserService.getUsersByRoleId(roleId));
    }

    @Operation(summary = "查询菜单树")
    @SaCheckPermission("system:role:list")
    @GetMapping("/menu-tree")
    public R<List<SysMenu>> menuTree() {
        return R.ok(sysMenuService.listMenuTree());
    }
}