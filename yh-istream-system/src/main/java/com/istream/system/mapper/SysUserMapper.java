package com.istream.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.istream.system.model.query.SysUserQuery;
import com.istream.system.entity.SysUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysUserMapper extends BaseMapper<SysUser> {

    IPage<SysUser> selectUserPage(Page<SysUser> page, @Param("query") SysUserQuery query);

    /**
     * 根据角色ID查询关联用户列表，避免 N+1 性能问题
     *
     * @param roleId 角色ID
     * @return 用户列表
     */
    List<SysUser> selectUsersByRoleId(@Param("roleId") Long roleId);
}