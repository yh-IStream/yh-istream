package com.istream.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.spring.service.IService;
import com.istream.common.model.query.SysRoleQuery;
import com.istream.system.entity.SysRole;

import java.util.List;

public interface SysRoleService extends IService<SysRole> {

    IPage<SysRole> page(SysRoleQuery query);

    boolean changeStatus(Long roleId, Integer status);

    List<Long> getMenuIdsByRoleId(Long roleId);

    void saveRoleMenu(Long roleId, List<Long> menuIds);

    void assignUsersToRole(Long roleId, List<Long> userIds);

    /** 获取角色已授权的部门ID列表（用于自定义数据范围） */
    List<Long> getDeptIdsByRoleId(Long roleId);

    /** 保存角色自定义数据范围授权的部门（先删后插） */
    void saveRoleDept(Long roleId, List<Long> deptIds);

    boolean hasUsers(Long roleId);

    boolean hasUsersAny(List<Long> roleIds);
}