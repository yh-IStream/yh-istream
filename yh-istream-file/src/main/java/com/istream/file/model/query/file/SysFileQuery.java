package com.istream.file.model.query.file;

import com.istream.common.model.query.BaseQuery;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文件查询条件
 *
 * @author istream
 * @since 2026-09-08
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysFileQuery extends BaseQuery {

    private String originalName;

    private String fileExt;
}