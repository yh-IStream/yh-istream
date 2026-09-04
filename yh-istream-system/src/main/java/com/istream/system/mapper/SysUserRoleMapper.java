package com.istream.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.istream.system.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    void insertBatch(List<SysUserRole> list);
}