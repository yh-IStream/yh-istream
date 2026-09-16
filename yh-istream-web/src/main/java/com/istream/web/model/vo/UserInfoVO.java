package com.istream.web.model.vo;

import com.istream.system.entity.SysMenu;
import com.istream.system.model.dto.SysUserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 当前登录用户信息响应 VO
 *
 * @author istream
 * @since 2026-08-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private SysUserDTO user;

    private List<String> permissions;

    private List<String> roles;

    private List<SysMenu> menus;
}