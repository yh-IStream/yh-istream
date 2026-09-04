package com.istream.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.spring.service.IService;
import com.istream.common.model.query.SysUserQuery;
import com.istream.system.entity.SysRole;
import com.istream.system.entity.SysUser;

import java.util.List;

public interface SysUserService extends IService<SysUser> {

    SysUser getByUsername(String username);

    IPage<SysUser> page(SysUserQuery query);

    void createUser(SysUser user);

    void updateUser(SysUser user);

    void assignUserRoles(Long userId, List<Long> roleIds);

    boolean resetPassword(Long userId, String newPassword);

    boolean changeStatus(Long userId, Integer status);

    List<SysUser> getUsersByRoleId(Long roleId);

    List<SysRole> getRolesByUserId(Long userId);

    boolean removeById(Long id);

    void updateLoginInfo(Long userId, String ip);

    void updateLoginFailCount(Long userId, int failCount);
}