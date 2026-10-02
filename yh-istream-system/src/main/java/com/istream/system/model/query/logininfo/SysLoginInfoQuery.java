package com.istream.system.model.query.logininfo;

import com.istream.common.model.query.BaseQuery;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysLoginInfoQuery extends BaseQuery {

    private String username;

    private String ipAddress;

    private Integer status;
}