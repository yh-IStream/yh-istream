package com.istream.system.model.query.config;

import com.istream.common.model.query.BaseQuery;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysConfigQuery extends BaseQuery {

    private String configName;

    private String configKey;
}