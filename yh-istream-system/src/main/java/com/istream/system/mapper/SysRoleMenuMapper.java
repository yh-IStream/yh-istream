package com.istream.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.istream.system.entity.SysRoleMenu;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {

    void insertBatch(List<SysRoleMenu> list);
}