package com.istream.generator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 表元数据信息
 *
 * @author isteam
 * @since 2026-08-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 表名 */
    private String tableName;

    /** 表注释 */
    private String tableComment;

    /** 实体类名 */
    private String className;

    /** 列信息列表 */
    private List<ColumnInfo> columns;

    /** 创建时间 */
    private String createTime;
}