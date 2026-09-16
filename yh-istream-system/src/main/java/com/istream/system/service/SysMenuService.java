package com.istream.system.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.istream.system.entity.SysMenu;

import java.util.List;

/**
 * 菜单服务接口
 *
 * <p>提供菜单树构建、权限查询、当前用户菜单等业务方法。</p>
 *
 * @author istream
 * @since 2026-09-08
 */
public interface SysMenuService extends IService<SysMenu> {

    /**
     * 根据用户ID查询权限标识列表
     *
     * @param userId 用户ID
     * @return 权限标识列表
     */
    List<String> getPermissionsByUserId(Long userId);

    /**
     * 查询菜单树（不含按钮，用于角色菜单分配）
     *
     * @return 菜单树
     */
    List<SysMenu> listMenuTree();

    /**
     * 查询所有菜单树（含按钮，用于菜单管理页面）
     *
     * @return 菜单树
     */
    List<SysMenu> listAllMenuTree();

    /**
     * 查询当前登录用户的菜单树（侧边栏用）
     *
     * @return 菜单树
     */
    List<SysMenu> getCurrentUserMenuTree();

    /**
     * 判断菜单是否存在子菜单
     *
     * @param menuId 菜单ID
     * @return 是否存在
     */
    boolean hasChildren(Long menuId);

    /**
     * 判断菜单是否被角色引用
     *
     * @param menuId 菜单ID
     * @return 是否被引用
     */
    boolean hasRoles(Long menuId);
}