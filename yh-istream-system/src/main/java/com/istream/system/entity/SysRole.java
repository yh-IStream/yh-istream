package com.istream.system.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableName;
import com.istream.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ExcelProperty("角色名称")
    private String roleName;

    @ExcelProperty("角色标识")
    private String roleKey;

    @ExcelProperty("排序")
    private Integer roleSort;

    @ExcelIgnore
    private Integer dataScope;

    @ExcelProperty("状态")
    private Integer status;
}