package com.istream.system.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.istream.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_data")
public class SysDictData extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String dictType;

    private String dictLabel;

    private String dictValue;

    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String cssClass;

    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String listClass;

    private Integer isDefault;

    private Integer orderNum;

    private Integer status;
}