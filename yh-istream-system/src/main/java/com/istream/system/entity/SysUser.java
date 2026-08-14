package com.istream.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.istream.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableField("dept_id")
    private Long deptId;

    private String username;

    private String password;

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

    @TableField(exist = false)
    private SysDept dept;

    @TableField(exist = false)
    private java.util.List<SysRole> roles;
}