package com.istream.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.istream.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_type")
public class SysDictType extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String dictName;

    private String dictType;

    private Integer status;
}