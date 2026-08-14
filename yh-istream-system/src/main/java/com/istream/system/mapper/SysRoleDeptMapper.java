package com.istream.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.istream.system.entity.SysRoleDept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysRoleDeptMapper extends BaseMapper<SysRoleDept> {

    @Select("SELECT dept_id FROM sys_role_dept WHERE role_id = #{roleId}")
    List<Long> selectDeptIdsByRoleId(Long roleId);
}