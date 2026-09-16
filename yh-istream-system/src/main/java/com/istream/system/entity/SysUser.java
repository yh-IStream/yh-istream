package com.istream.system.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.istream.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ExcelIgnore
    @TableField("dept_id")
    private Long deptId;

    @ExcelProperty("用户名")
    private String username;

    @ExcelIgnore
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @ExcelProperty("昵称")
    private String nickname;

    @ExcelProperty("邮箱")
    private String email;

    @ExcelProperty("手机号")
    private String phone;

    @ExcelProperty("性别")
    private Integer gender;

    @ExcelIgnore
    private String avatar;

    @ExcelProperty("状态")
    private Integer status;

    @ExcelProperty("登录IP")
    private String loginIp;

    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ExcelProperty("登录时间")
    private LocalDateTime loginDate;

    @ExcelProperty("登录次数")
    private Integer loginCount;

    @ExcelIgnore
    private Integer loginFailCount;

    @ExcelIgnore
    private LocalDateTime pwdResetTime;

    @ExcelProperty("部门")
    @TableField(exist = false)
    private SysDept dept;

    @ExcelIgnore
    @TableField(exist = false)
    private List<SysRole> roles;

    @ExcelIgnore
    @TableField(exist = false)
    private List<Long> roleIds;
}