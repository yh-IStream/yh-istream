package com.istream.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.istream.common.annotation.OperLog;
import com.istream.common.constant.Constants;
import com.istream.common.enums.BusinessType;
import com.istream.common.enums.StatusEnum;
import com.istream.system.model.query.role.SysRoleQuery;
import com.istream.common.validation.Groups;
import com.istream.system.model.dto.role.SysRoleSaveDTO;
import com.istream.system.model.dto.role.SysRoleDTO;
import com.istream.system.model.dto.user.SysUserDTO;
import com.istream.common.model.R;
import com.istream.system.model.vo.role.SysRoleMenuTreeVO;
import com.istream.common.enums.ResultCode;
import com.istream.framework.util.ExcelExportUtil;
import com.istream.framework.util.PageUtils;
import com.istream.system.converter.SysMenuConverter;
import com.istream.system.converter.SysRoleConverter;
import com.istream.system.converter.SysUserConverter;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
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
import java.util.List;

/**
 * 角色管理控制器
 *
 * @author istream
 * @since 2026-08-17
 */
@Slf4j
@Tag(name = "角色管理")
@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService sysRoleService;
    private final SysMenuService sysMenuService;
    private final SysUserService sysUserService;
    private final SysUserConverter sysUserConverter;
    private final SysRoleConverter sysRoleConverter;
    private final SysMenuConverter sysMenuConverter;

    @Operation(summary = "分页查询角色列表")
    @SaCheckPermission("system:role:list")
    @GetMapping("/list")
    public R<IPage<SysRoleDTO>> list(SysRoleQuery query) {
        IPage<SysRole> page = sysRoleService.page(query);
        System.out.println("SOUT: " + page);
        log.info("LOG : {}", page);
        return R.ok(PageUtils.toDtoPage(page, sysRoleConverter::toDto));
    }

    @Operation(summary = "查询所有角色（下拉选择用）")
    @SaCheckPermission("system:role:list")
    @GetMapping("/all")
    public R<List<SysRoleDTO>> all() {
        return R.ok(sysRoleService.listAllEnabled().stream()
                .map(sysRoleConverter::toDto)
                .toList());
    }

    @Operation(summary = "根据ID查询角色")
    @SaCheckPermission("system:role:query")
    @GetMapping("/{id}")
    public R<SysRoleDTO> getById(@PathVariable Long id) {
        SysRole role = sysRoleService.getById(id);
        return R.ok(sysRoleConverter.toDto(role));
    }

    @OperLog(title = "角色管理", businessType = BusinessType.INSERT)
    @Operation(summary = "新增角色")
    @SaCheckPermission("system:role:add")
    @PostMapping
    public R<Long> add(@Validated(Groups.Create.class) @RequestBody SysRoleSaveDTO dto) {
        if (sysRoleService.existsByRoleKey(dto.getRoleKey(), null)) {
            return R.fail(ResultCode.DATA_DUPLICATE, "角色标识已存在");
        }
        SysRole role = sysRoleConverter.toEntity(dto);
        sysRoleService.save(role);
        return R.ok(role.getId());
    }

    @OperLog(title = "角色管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改角色")
    @SaCheckPermission("system:role:edit")
    @PutMapping
    public R<Void> update(@Validated(Groups.Update.class) @RequestBody SysRoleSaveDTO dto) {
        if (dto.getRoleKey() != null && sysRoleService.existsByRoleKey(dto.getRoleKey(), dto.getId())) {
            return R.fail(ResultCode.DATA_DUPLICATE, "角色标识已存在");
        }
        SysRole role = new SysRole();
        role.setId(dto.getId());
        sysRoleConverter.updateEntity(role, dto);
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
        if (StatusEnum.isInvalidCode(status)) {
            return R.fail(ResultCode.PARAM_VALID_ERROR, "状态值无效");
        }
        sysRoleService.changeStatus(roleId, status);
        return R.ok();
    }

    @Operation(summary = "查询角色菜单树及已选菜单ID")
    @SaCheckPermission("system:role:edit")
    @GetMapping("/menu-tree/{roleId}")
    public R<SysRoleMenuTreeVO> menuTree(@PathVariable Long roleId) {
        List<SysMenu> menus = sysMenuService.listMenuTree();
        List<Long> checkedKeys = sysRoleService.getMenuIdsByRoleId(roleId);
        return R.ok(new SysRoleMenuTreeVO(sysMenuConverter.toDtoList(menus), checkedKeys));
    }

    @OperLog(title = "角色管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "保存角色菜单分配")
    @SaCheckPermission("system:role:edit")
    @PutMapping("/{roleId}/menu-assign")
    public R<Void> saveRoleMenu(@PathVariable Long roleId, @Valid @RequestBody List<Long> menuIds) {
        sysRoleService.saveRoleMenu(roleId, menuIds);
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
    @PutMapping("/{roleId}/dept-assign")
    public R<Void> saveRoleDept(@PathVariable Long roleId, @Valid @RequestBody List<Long> deptIds) {
        sysRoleService.saveRoleDept(roleId, deptIds != null ? deptIds : List.of());
        return R.ok();
    }

    @Operation(summary = "查询角色已分配的用户列表")
    @SaCheckPermission("system:role:edit")
    @GetMapping("/{roleId}/users")
    public R<List<SysUserDTO>> getUsers(@PathVariable Long roleId) {
        List<SysUser> users = sysUserService.getUsersByRoleId(roleId);
        return R.ok(users.stream().map(sysUserConverter::toDto).toList());
    }

    @OperLog(title = "角色管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "分配角色用户")
    @SaCheckPermission("system:role:edit")
    @PutMapping("/{roleId}/users")
    public R<Void> assignUsers(@PathVariable Long roleId, @Valid @RequestBody List<Long> userIds) {
        sysRoleService.assignUsersToRole(roleId, userIds);
        return R.ok();
    }

    @Operation(summary = "导出角色列表")
    @SaCheckPermission("system:role:export")
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws IOException {
        ExcelExportUtil.exportByPage(response, "角色列表", "角色列表", SysRole.class,
                (pageNum) -> sysRoleService.pageExport(pageNum, Constants.EXPORT_PAGE_SIZE));
    }
}