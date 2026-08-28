package com.istream.system.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.istream.system.entity.SysMenu;

import java.util.List;

public interface SysMenuService extends IService<SysMenu> {

    List<String> getPermissionsByUserId(Long userId);

    List<SysMenu> listMenuTree();

    List<SysMenu> listAllMenuTree();

    List<SysMenu> getCurrentUserMenuTree();

    boolean hasChildren(Long menuId);

    boolean hasRoles(Long menuId);
}