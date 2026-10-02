package com.istream.generator.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 列元数据信息 VO
 *
 * @author istream
 * @since 2026-08-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColumnInfoVO {

    /** 数据库列名（下划线命名） */
    private String columnName;

    /** 列注释 */
    private String columnComment;

    /** Java 类型（短名，如 String、Long、Integer） */
    private String javaType;

    /** Java 字段名（驼峰命名） */
    private String javaField;

    /** 是否主键 */
    private boolean isPk;

    /** 是否必填 */
    private boolean isRequired;

    /** 是否为 BaseEntity 中已定义的字段 */
    private boolean isBaseField;

    /** SQL 类型（如 VARCHAR(64)） */
    private String sqlType;

    /** 是否可建索引 */
    private boolean isIndexable;
}