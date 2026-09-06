package com.istream.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.istream.common.annotation.OperLog;
import com.istream.common.annotation.RateLimit;
import com.istream.common.enums.BusinessType;
import com.istream.common.model.dto.SysUserDTO;
import com.istream.common.model.query.SysUserQuery;
import com.istream.common.model.dto.UserRoleAssignDTO;
import com.istream.common.model.R;
import com.istream.common.enums.ResultCode;
import com.istream.framework.util.ExcelExportUtil;
import com.istream.system.entity.SysUser;
import com.istream.system.converter.SysUserConverter;
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

/**
 * 用户管理控制器
 *
 * @author istream
 * @since 2026-08-17
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class SysUserController {

    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_PASSWORD_LENGTH = 32;
    private static final int EXPORT_PAGE_SIZE = 5000;

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
    public R<Void> assignRoles(@PathVariable Long id, @Valid @RequestBody UserRoleAssignDTO dto) {
        sysUserService.assignUserRoles(id, dto.getRoleIds() != null ? dto.getRoleIds() : List.of());
        return R.ok();
    }

    @OperLog(title = "用户管理", businessType = BusinessType.DELETE)
    @Operation(summary = "删除用户")
    @SaCheckPermission("system:user:delete")
    @DeleteMapping
    public R<Void> delete(@RequestBody List<Long> ids) {
        sysUserService.removeByIds(ids);
        return R.ok();
    }

    @OperLog(title = "用户管理", businessType = BusinessType.UPDATE)
    @RateLimit(key = "user:reset-pwd", rate = 3, timeout = 60)
    @Operation(summary = "重置密码")
    @SaCheckPermission("system:user:reset-pwd")
    @PutMapping("/reset-pwd")
    public R<Void> resetPassword(@RequestParam Long userId, @RequestParam String password) {
        if (password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
            return R.fail(ResultCode.PARAM_VALID_ERROR,
                    "密码长度必须在" + MIN_PASSWORD_LENGTH + "-" + MAX_PASSWORD_LENGTH + "位之间");
        }
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
    @SaCheckPermission("system:user:export")
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws IOException {
        ExcelExportUtil.exportByPage(response, "用户列表", "用户列表", SysUser.class,
                (pageNum) -> {
                    Page<SysUser> page = sysUserService.page(new Page<>(pageNum, EXPORT_PAGE_SIZE));
                    page.getRecords().forEach(user -> user.setPassword(null));
                    return page;
                });
    }
}