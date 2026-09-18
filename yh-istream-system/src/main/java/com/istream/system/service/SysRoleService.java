package com.istream.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.spring.service.IService;
import com.istream.system.model.query.role.SysRoleQuery;
import com.istream.system.entity.SysRole;

import java.util.List;

/**
 * 角色服务接口
 *
 * <p>提供角色 CRUD、状态变更、菜单分配、部门数据范围授权、用户分配等业务方法。</p>
 *
 * @author istream
 * @since 2026-08-17
 */
public interface SysRoleService extends IService<SysRole> {

    /**
     * 分页查询角色列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<SysRole> page(SysRoleQuery query);

    /**
     * 变更角色状态
     *
     * @param roleId 角色ID
     * @param status 状态值（0=启用 1=禁用）
     * @return 是否成功
     */
    boolean changeStatus(Long roleId, Integer status);

    /**
     * 获取角色已分配的菜单ID列表
     *
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    List<Long> getMenuIdsByRoleId(Long roleId);

    /**
     * 保存角色菜单分配（先删后插）
     *
     * @param roleId  角色ID
     * @param menuIds 菜单ID列表
     */
    void saveRoleMenu(Long roleId, List<Long> menuIds);

    /**
     * 分配用户到角色
     *
     * @param roleId  角色ID
     * @param userIds 用户ID列表
     */
    void assignUsersToRole(Long roleId, List<Long> userIds);

    /**
     * 获取角色已授权的部门ID列表（用于自定义数据范围）
     *
     * @param roleId 角色ID
     * @return 部门ID列表
     */
    List<Long> getDeptIdsByRoleId(Long roleId);

    /**
     * 保存角色自定义数据范围授权的部门（先删后插）
     *
     * @param roleId  角色ID
     * @param deptIds 部门ID列表
     */
    void saveRoleDept(Long roleId, List<Long> deptIds);

    /**
     * 查询所有启用且未删除的角色（下拉选择用）
     *
     * @return 角色列表
     */
    List<SysRole> listAllEnabled();

    /**
     * 判断角色标识是否已存在
     *
     * @param roleKey 角色标识
     * @param excludeId 排除的角色ID（修改时排除自身，可为null）
     * @return 是否存在
     */
    boolean existsByRoleKey(String roleKey, Long excludeId);

    /**
     * 判断角色下是否存在用户
     *
     * @param roleId 角色ID
     * @return 是否存在
     */
    boolean hasUsers(Long roleId);

    /**
     * 判断任意角色下是否存在用户
     *
     * @param roleIds 角色ID列表
     * @return 是否存在
     */
    boolean hasUsersAny(List<Long> roleIds);

    /**
     * 查询全部角色（导出用，按创建时间倒序）
     *
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    IPage<SysRole> pageExport(long pageNum, long pageSize);
}