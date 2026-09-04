package com.istream.common.model.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysConfigQuery extends BaseQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private String configName;

    private String configKey;
}