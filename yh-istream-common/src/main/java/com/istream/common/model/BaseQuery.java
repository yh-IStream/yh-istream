package com.istream.common.model;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 查询基类
 */
@Data
public class BaseQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 排序字段 */
    private String orderBy;

    /** 排序方向（asc/desc） */
    private String orderDirection;

    /** 当前页码 */
    private Long pageNum = 1L;

    /** 每页条数 */
    private Long pageSize = 10L;

    /** 扩展参数（用于数据权限 SQL 注入等场景） */
    private Map<String, Object> params = new HashMap<>();
}