package com.istream.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.spring.service.IService;
import com.istream.common.model.dto.SysUserQuery;
import com.istream.system.entity.SysUser;

import java.util.List;

public interface SysUserService extends IService<SysUser> {

    SysUser getByUsername(String username);

    IPage<SysUser> page(SysUserQuery query);

    void createUser(SysUser user);

    void updateUser(SysUser user);

    boolean resetPassword(Long userId, String newPassword);

    boolean changeStatus(Long userId, Integer status);

    List<SysUser> getUsersByRoleId(Long roleId);

    void updateLoginInfo(Long userId, String ip);

    void updateLoginFailCount(Long userId, int failCount);
}