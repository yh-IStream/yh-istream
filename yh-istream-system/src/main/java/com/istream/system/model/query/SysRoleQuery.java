package com.istream.system.model.query;

import com.istream.common.model.query.BaseQuery;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysRoleQuery extends BaseQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private String roleName;

    private String roleKey;

    private Integer status;
}