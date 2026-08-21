package com.istream.common.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户信息 DTO（不含密码，用于列表/详情返回）
 *
 * @author isteam
 * @since 2026-08-17
 */
@Data
public class SysUserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long deptId;

    private String username;

    private String nickname;

    private String email;

    private String phone;

    private Integer gender;

    private String avatar;

    private Integer status;

    private String loginIp;

    private LocalDateTime loginDate;

    private Integer loginCount;

    private LocalDateTime pwdResetTime;

    private LocalDateTime createTime;

    private String deptName;

    private List<String> roleNames;
}