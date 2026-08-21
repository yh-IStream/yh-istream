package com.istream.generator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 列元数据信息
 *
 * @author isteam
 * @since 2026-08-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColumnInfo implements Serializable {

    private static final long serialVersionUID = 1L;

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

    /** 是否必填（NOT NULL） */
    private boolean isRequired;

    /** 是否 BaseEntity 已有字段 */
    private boolean isBaseField;

    /** SQL 列类型（如 VARCHAR(255)、INT、BIGINT） */
    private String sqlType;

    /** 是否可为索引列 */
    private boolean isIndexable;
}