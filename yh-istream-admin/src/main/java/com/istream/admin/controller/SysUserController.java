package com.istream.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.istream.common.model.dto.SysUserQuery;
import com.istream.common.model.R;
import com.istream.common.enums.ResultCode;
import com.istream.system.entity.SysUser;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;

    @Operation(summary = "分页查询用户列表")
    @SaCheckPermission("system:user:list")
    @GetMapping("/list")
    public R<IPage<SysUser>> list(SysUserQuery query) {
        return R.ok(sysUserService.page(query));
    }

    @Operation(summary = "根据ID查询用户")
    @SaCheckPermission("system:user:query")
    @GetMapping("/{id}")
    public R<SysUser> getById(@PathVariable Long id) {
        SysUser user = sysUserService.getById(id);
        if (user == null) {
            return R.fail(ResultCode.USER_NOT_EXIST);
        }
        user.setPassword(null);
        return R.ok(user);
    }

    @Operation(summary = "新增用户")
    @SaCheckPermission("system:user:add")
    @PostMapping
    public R<Void> add(@RequestBody SysUser user) {
        sysUserService.createUser(user);
        return R.ok();
    }

    @Operation(summary = "修改用户")
    @SaCheckPermission("system:user:edit")
    @PutMapping
    public R<Void> update(@RequestBody SysUser user) {
        sysUserService.updateUser(user);
        return R.ok();
    }

    @Operation(summary = "删除用户")
    @SaCheckPermission("system:user:delete")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        sysUserService.removeById(id);
        return R.ok();
    }

    @Operation(summary = "批量删除用户")
    @SaCheckPermission("system:user:delete")
    @DeleteMapping("/batch")
    public R<Void> deleteBatch(@RequestBody List<Long> ids) {
        sysUserService.removeByIds(ids);
        return R.ok();
    }

    @Operation(summary = "重置密码")
    @SaCheckPermission("system:user:reset-pwd")
    @PutMapping("/reset-pwd")
    public R<Void> resetPassword(@RequestParam Long userId, @RequestParam String password) {
        sysUserService.resetPassword(userId, password);
        return R.ok();
    }

    @Operation(summary = "修改用户状态")
    @SaCheckPermission("system:user:edit")
    @PutMapping("/change-status")
    public R<Void> changeStatus(@RequestParam Long userId, @RequestParam Integer status) {
        sysUserService.changeStatus(userId, status);
        return R.ok();
    }
}