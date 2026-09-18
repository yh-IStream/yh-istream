package com.istream.system.model.query.logininfo;

import com.istream.common.model.query.BaseQuery;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysLoginInfoQuery extends BaseQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private String username;

    private String ipAddress;

    private Integer status;
}