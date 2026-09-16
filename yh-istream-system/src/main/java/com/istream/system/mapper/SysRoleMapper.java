package com.istream.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.istream.system.model.query.SysRoleQuery;
import com.istream.system.entity.SysRole;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysRoleMapper extends BaseMapper<SysRole> {

    List<String> selectRoleKeysByUserId(@Param("userId") Long userId);

    IPage<SysRole> selectRolePage(Page<SysRole> page, @Param("query") SysRoleQuery query);
}