package com.istream.system.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 角色菜单树响应 VO（包含菜单树 + 已选菜单ID）
 *
 * @author istream
 * @since 2026-08-17
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleMenuTreeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<?> menus;

    private List<Long> checkedKeys;
}