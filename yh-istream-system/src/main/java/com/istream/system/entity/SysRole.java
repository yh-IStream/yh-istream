package com.istream.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.istream.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String roleName;

    private String roleKey;

    private Integer roleSort;

    private Integer dataScope;

    private Integer status;
}