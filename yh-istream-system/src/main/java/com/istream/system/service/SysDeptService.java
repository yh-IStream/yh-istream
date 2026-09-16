package com.istream.system.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.istream.system.entity.SysDept;

import java.util.List;

/**
 * 部门服务接口
 *
 * <p>提供部门树构建、子部门/用户判断等业务方法。</p>
 *
 * @author istream
 * @since 2026-08-17
 */
public interface SysDeptService extends IService<SysDept> {

    /**
     * 查询部门树
     *
     * @return 部门树
     */
    List<SysDept> listDeptTree();

    /**
     * 判断部门是否存在子部门
     *
     * @param deptId 部门ID
     * @return 是否存在
     */
    boolean hasChildren(Long deptId);

    /**
     * 判断部门下是否存在用户
     *
     * @param deptId 部门ID
     * @return 是否存在
     */
    boolean hasUsers(Long deptId);
}