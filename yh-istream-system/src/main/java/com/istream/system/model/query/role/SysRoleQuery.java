package com.istream.system.model.query.role;

import com.istream.common.model.query.BaseQuery;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysRoleQuery extends BaseQuery {

    private String roleName;

    private String roleKey;

    private Integer status;
}