package com.istream.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.istream.common.annotation.OperLog;
import com.istream.common.enums.BusinessType;
import com.istream.common.model.query.SysRoleQuery;
import com.istream.common.model.dto.RoleMenuAssignDTO;
import com.istream.common.model.dto.RoleDeptAssignDTO;
import com.istream.common.model.R;
import com.istream.common.enums.ResultCode;
import com.istream.framework.util.ExcelExportUtil;
import com.istream.system.entity.SysRole;
import com.istream.system.entity.SysMenu;
import com.istream.system.entity.SysUser;
import com.istream.system.service.SysMenuService;
import com.istream.system.service.SysRoleService;
import com.istream.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @OperLog(title = "角色管理", businessType = BusinessType.INSERT)
    @Operation(summary = "新增角色")
    @SaCheckPermission("system:role:add")
    @PostMapping
    public R<Long> add(@Valid @RequestBody SysRole role) {
        if (sysRoleService.count(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleKey, role.getRoleKey())) > 0) {
            return R.fail(ResultCode.DATA_DUPLICATE, "角色标识已存在");
        }
        role.setId(null);
        sysRoleService.save(role);
        return R.ok(role.getId());
    }

    @OperLog(title = "角色管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改角色")
    @SaCheckPermission("system:role:edit")
    @PutMapping
    public R<Void> update(@Valid @RequestBody SysRole role) {
        SysRole exist = sysRoleService.getOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleKey, role.getRoleKey()));
        if (exist != null && !exist.getId().equals(role.getId())) {
            return R.fail(ResultCode.DATA_DUPLICATE, "角色标识已存在");
        }
        sysRoleService.updateById(role);
        return R.ok();
    }

    @OperLog(title = "角色管理", businessType = BusinessType.DELETE)
    @Operation(summary = "删除角色")
    @SaCheckPermission("system:role:delete")
    @DeleteMapping
    public R<Void> delete(@RequestBody List<Long> ids) {
        if (sysRoleService.hasUsersAny(ids)) {
            return R.fail(ResultCode.HAS_USERS);
        }
        sysRoleService.removeByIds(ids);
        return R.ok();
    }

    @OperLog(title = "角色管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改角色状态")
    @SaCheckPermission("system:role:edit")
    @PutMapping("/change-status")
    public R<Void> changeStatus(@RequestParam Long roleId, @RequestParam Integer status) {
        sysRoleService.changeStatus(roleId, status);
        return R.ok();
    }

    @Operation(summary = "查询角色菜单树及已选菜单ID")
    @SaCheckPermission("system:role:edit")
    @GetMapping("/menu-tree/{roleId}")
    public R<Map<String, Object>> menuTree(@PathVariable Long roleId) {
        List<SysMenu> menus = sysMenuService.listMenuTree();
        List<Long> checkedKeys = sysRoleService.getMenuIdsByRoleId(roleId);
        Map<String, Object> result = new HashMap<>();
        result.put("menus", menus);
        result.put("checkedKeys", checkedKeys);
        return R.ok(result);
    }

    @OperLog(title = "角色管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "保存角色菜单分配")
    @SaCheckPermission("system:role:edit")
    @PutMapping("/menu-assign")
    public R<Void> saveRoleMenu(@Valid @RequestBody RoleMenuAssignDTO dto) {
        sysRoleService.saveRoleMenu(dto.getRoleId(), dto.getMenuIds());
        return R.ok();
    }

    @Operation(summary = "查询角色已授权的部门ID列表（自定义数据范围）")
    @SaCheckPermission("system:role:edit")
    @GetMapping("/{roleId}/depts")
    public R<List<Long>> getRoleDepts(@PathVariable Long roleId) {
        return R.ok(sysRoleService.getDeptIdsByRoleId(roleId));
    }

    @OperLog(title = "角色管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "保存角色自定义数据范围授权的部门")
    @SaCheckPermission("system:role:edit")
    @PutMapping("/dept-assign")
    public R<Void> saveRoleDept(@Valid @RequestBody RoleDeptAssignDTO dto) {
        sysRoleService.saveRoleDept(dto.getRoleId(), dto.getDeptIds() != null ? dto.getDeptIds() : List.of());
        return R.ok();
    }

    @Operation(summary = "查询角色已分配的用户列表")
    @SaCheckPermission("system:role:edit")
    @GetMapping("/{roleId}/users")
    public R<List<SysUser>> getUsers(@PathVariable Long roleId) {
        List<SysUser> users = sysUserService.getUsersByRoleId(roleId);
        users.forEach(user -> user.setPassword(null));
        return R.ok(users);
    }

    @OperLog(title = "角色管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "分配角色用户")
    @SaCheckPermission("system:role:edit")
    @PutMapping("/{roleId}/users")
    public R<Void> assignUsers(@PathVariable Long roleId, @RequestBody List<Long> userIds) {
        sysRoleService.assignUsersToRole(roleId, userIds);
        return R.ok();
    }

    @Operation(summary = "导出角色列表")
    @SaCheckPermission("system:role:export")
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws IOException {
        List<SysRole> list = sysRoleService.list();
        ExcelExportUtil.export(response, "角色列表", "角色列表", SysRole.class, list);
    }
}