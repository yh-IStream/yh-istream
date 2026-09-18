package com.istream.generator.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 代码生成请求 DTO
 *
 * @author istream
 * @since 2026-08-17
 */
@Data
public class GenRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 表名列表 */
    private List<String> tableNames;

    /** 作者 */
    private String author = "istream";

    /** 模块名（如 system、oss） */
    private String moduleName = "system";

    /** 基础包名 */
    private String packageName = "com.istream.system";

    /** Controller 包名 */
    private String controllerPackage = "com.istream.web.controller";

    /** 是否生成数据库迁移文件 */
    private boolean genMigration = true;
}