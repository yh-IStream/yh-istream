package com.istream.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.istream.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dept")
public class SysDept extends BaseEntity {

    private Long parentId;

    private String ancestors;

    private String deptName;

    private Integer orderNum;

    private String leader;

    private String phone;

    private String email;

    private Integer status;

    @TableField(exist = false)
    private List<SysDept> children = new ArrayList<>();
}