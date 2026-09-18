package com.istream.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.spring.service.IService;
import com.istream.system.model.dto.user.SysUserSaveDTO;
import com.istream.system.model.query.user.SysUserQuery;
import com.istream.system.entity.SysRole;
import com.istream.system.entity.SysUser;

import java.util.List;

/**
 * 用户服务接口
 *
 * <p>提供用户 CRUD、角色分配、密码重置、状态变更、登录信息更新等业务方法。</p>
 *
 * @author istream
 * @since 2026-08-17
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户实体，不存在返回 null
     */
    SysUser getByUsername(String username);

    /**
     * 分页查询用户列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<SysUser> page(SysUserQuery query);

    /**
     * 创建用户
     *
     * @param dto 用户新增 DTO
     */
    void createUser(SysUserSaveDTO dto);

    /**
     * 更新用户
     *
     * @param dto 用户修改 DTO
     */
    void updateUser(SysUserSaveDTO dto);

    /**
     * 分配用户角色
     *
     * @param userId  用户ID
     * @param roleIds 角色ID列表
     */
    void assignUserRoles(Long userId, List<Long> roleIds);

    /**
     * 重置用户密码
     *
     * @param userId      用户ID
     * @param newPassword 新密码（明文，内部 BCrypt 加密）
     * @return 是否成功
     */
    boolean resetPassword(Long userId, String newPassword);

    /**
     * 变更用户状态
     *
     * @param userId 用户ID
     * @param status 状态值（0=启用 1=禁用）
     * @return 是否成功
     */
    boolean changeStatus(Long userId, Integer status);

    /**
     * 查询角色下的用户列表
     *
     * @param roleId 角色ID
     * @return 用户列表
     */
    List<SysUser> getUsersByRoleId(Long roleId);

    /**
     * 查询用户的角色列表
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    List<SysRole> getRolesByUserId(Long userId);

    /**
     * 清理用户孤立的角色关联
     *
     * @param userId 用户ID
     */
    void cleanOrphanedUserRoles(Long userId);

    /**
     * 删除用户（逻辑删除）
     *
     * @param id 用户ID
     * @return 是否成功
     */
    boolean removeById(Long id);

    /**
     * 更新用户登录信息（IP、时间、次数）
     *
     * @param userId 用户ID
     * @param ip     登录IP
     */
    void updateLoginInfo(Long userId, String ip);

    /**
     * 更新登录失败次数
     *
     * @param userId    用户ID
     * @param failCount 失败次数
     */
    void updateLoginFailCount(Long userId, int failCount);

    /**
     * 查询全部用户（导出用，按创建时间倒序，密码置空）
     *
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果（密码字段已置空）
     */
    IPage<SysUser> pageExport(long pageNum, long pageSize);
}