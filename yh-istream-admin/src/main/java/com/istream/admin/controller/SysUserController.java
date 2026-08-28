package com.istream.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.istream.common.annotation.OperLog;
import com.istream.common.enums.BusinessType;
import com.istream.common.model.dto.SysUserDTO;
import com.istream.common.model.dto.SysUserQuery;
import com.istream.common.model.R;
import com.istream.common.enums.ResultCode;
import com.istream.framework.util.ExcelExportUtil;
import com.istream.system.entity.SysUser;
import com.istream.system.convert.SysUserConverter;
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
import java.util.List;
import java.util.Map;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;
    private final SysUserConverter sysUserConverter;

    @Operation(summary = "分页查询用户列表")
    @SaCheckPermission("system:user:list")
    @GetMapping("/list")
    public R<IPage<SysUserDTO>> list(SysUserQuery query) {
        IPage<SysUser> page = sysUserService.page(query);
        IPage<SysUserDTO> dtoPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        dtoPage.setRecords(page.getRecords().stream()
                .map(sysUserConverter::toDto)
                .toList());
        return R.ok(dtoPage);
    }

    @Operation(summary = "根据ID查询用户")
    @SaCheckPermission("system:user:query")
    @GetMapping("/{id}")
    public R<SysUserDTO> getById(@PathVariable Long id) {
        SysUser user = sysUserService.getById(id);
        if (user == null) {
            return R.fail(ResultCode.USER_NOT_EXIST);
        }
        user.setRoles(sysUserService.getRolesByUserId(id));
        return R.ok(sysUserConverter.toDto(user));
    }

    @OperLog(title = "用户管理", businessType = BusinessType.INSERT)
    @Operation(summary = "新增用户")
    @SaCheckPermission("system:user:add")
    @PostMapping
    public R<Void> add(@Valid @RequestBody SysUser user) {
        sysUserService.createUser(user);
        return R.ok();
    }

    @OperLog(title = "用户管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改用户")
    @SaCheckPermission("system:user:edit")
    @PutMapping
    public R<Void> update(@Valid @RequestBody SysUser user) {
        if (StrUtil.isNotBlank(user.getPassword())) {
            return R.fail(ResultCode.PARAM_VALID_ERROR, "不允许通过此接口修改密码，请使用密码重置接口");
        }
        sysUserService.updateUser(user);
        return R.ok();
    }

    @OperLog(title = "用户管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "分配用户角色")
    @SaCheckPermission("system:user:edit")
    @PutMapping("/{id}/roles")
    public R<Void> assignRoles(@PathVariable Long id, @RequestBody Map<String, List<Long>> body) {
        List<Long> roleIds = body.getOrDefault("roleIds", List.of());
        sysUserService.assignUserRoles(id, roleIds);
        return R.ok();
    }

    @OperLog(title = "用户管理", businessType = BusinessType.DELETE)
    @Operation(summary = "删除用户")
    @SaCheckPermission("system:user:delete")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        sysUserService.removeById(id);
        return R.ok();
    }

    @OperLog(title = "用户管理", businessType = BusinessType.DELETE)
    @Operation(summary = "批量删除用户")
    @SaCheckPermission("system:user:delete")
    @DeleteMapping("/batch")
    public R<Void> deleteBatch(@RequestBody List<Long> ids) {
        sysUserService.removeByIds(ids);
        return R.ok();
    }

    @OperLog(title = "用户管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "重置密码")
    @SaCheckPermission("system:user:reset-pwd")
    @PutMapping("/reset-pwd")
    public R<Void> resetPassword(@RequestParam Long userId, @RequestParam String password) {
        sysUserService.resetPassword(userId, password);
        return R.ok();
    }

    @OperLog(title = "用户管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改用户状态")
    @SaCheckPermission("system:user:edit")
    @PutMapping("/change-status")
    public R<Void> changeStatus(@RequestParam Long userId, @RequestParam Integer status) {
        sysUserService.changeStatus(userId, status);
        return R.ok();
    }

    @Operation(summary = "导出用户列表")
    @SaCheckPermission("system:user:list")
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws IOException {
        List<SysUser> list = sysUserService.list();
        list.forEach(user -> user.setPassword(null));
        ExcelExportUtil.export(response, "用户列表", "用户列表", SysUser.class, list);
    }
}