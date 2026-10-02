package com.istream.system.model.query.operlog;

import com.istream.common.model.query.BaseQuery;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysOperLogQuery extends BaseQuery {

    private String title;

    private Integer businessType;

    private Integer status;
}