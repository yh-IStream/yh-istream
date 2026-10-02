package com.istream.common.model.query;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 分页查询基类
 *
 * <p>所有列表查询 DTO 的公共基类，包含分页、排序和扩展参数。
 * 扩展参数 {@code params} 用于数据权限 SQL 注入等场景。</p>
 *
 * <p>{@code params} 采用懒初始化，仅在首次访问时创建 HashMap，
 * 避免高频查询场景下不必要的对象分配和 GC 压力。</p>
 *
 * @author istream
 * @since 2026-08-17
 */
@Data
public class BaseQuery {

    /** 排序字段 */
    private String orderBy;

    /** 排序方向（asc/desc） */
    private String orderDirection;

    /** 当前页码 */
    private Long pageNum = 1L;

    /** 每页条数 */
    private Long pageSize = 10L;

    /** 扩展参数（用于数据权限 SQL 注入等场景，懒初始化） */
    private Map<String, Object> params;

    /**
     * 获取扩展参数，首次访问时懒初始化
     *
     * @return 扩展参数 Map
     */
    public Map<String, Object> getParams() {
        if (params == null) {
            params = new HashMap<>();
        }
        return params;
    }
}