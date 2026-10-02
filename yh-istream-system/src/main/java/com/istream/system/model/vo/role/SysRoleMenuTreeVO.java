package com.istream.system.model.vo.role;

import com.istream.system.model.dto.menu.SysMenuDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 角色菜单树响应 VO（包含菜单树 + 已选菜单ID）
 *
 * @author istream
 * @since 2026-09-18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysRoleMenuTreeVO {

    private List<SysMenuDTO> menus;

    private List<Long> checkedKeys;
}