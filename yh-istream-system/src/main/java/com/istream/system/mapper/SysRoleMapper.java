package com.istream.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.istream.common.model.query.SysRoleQuery;
import com.istream.system.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    @Select("SELECT r.role_key FROM sys_role r INNER JOIN sys_user_role ur ON r.id = ur.role_id WHERE ur.user_id = #{userId} AND r.del_flag = 0 AND r.status = 0 ORDER BY r.role_sort")
    List<String> selectRoleKeysByUserId(Long userId);

    IPage<SysRole> selectRolePage(Page<SysRole> page, @Param("query") SysRoleQuery query);
}