package com.istream.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.istream.system.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    @Select("SELECT DISTINCT m.permission FROM sys_menu m INNER JOIN sys_role_menu rm ON m.id = rm.menu_id INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id WHERE ur.user_id = #{userId} AND m.permission IS NOT NULL AND m.permission <> '' AND m.del_flag = 0 AND m.status = 0")
    List<String> selectPermissionsByUserId(Long userId);
}