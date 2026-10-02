package com.istream.system.model.query.user;

import com.istream.common.model.query.BaseQuery;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysUserQuery extends BaseQuery {

    private String username;

    private String nickname;

    private String phone;

    private Integer status;

    private Long deptId;

    private List<Long> deptIds;

    private LocalDateTime beginTime;

    private LocalDateTime endTime;
}