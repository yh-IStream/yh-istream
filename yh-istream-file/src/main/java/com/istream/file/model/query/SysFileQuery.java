package com.istream.file.model.query;

import com.istream.common.model.query.BaseQuery;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 文件查询条件
 *
 * @author istream
 * @since 2026-09-08
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysFileQuery extends BaseQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private String originalName;

    private String fileExt;
}