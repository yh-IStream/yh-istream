package com.istream.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.spring.service.IService;
import com.istream.common.model.dto.SysRoleQuery;
import com.istream.system.entity.SysRole;

import java.util.List;

public interface SysRoleService extends IService<SysRole> {

    IPage<SysRole> page(SysRoleQuery query);

    boolean changeStatus(Long roleId, Integer status);

    List<Long> getMenuIdsByRoleId(Long roleId);

    void saveRoleMenu(Long roleId, List<Long> menuIds);

    boolean hasUsers(Long roleId);

    boolean hasUsersAny(List<Long> roleIds);
}