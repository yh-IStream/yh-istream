package com.istream.generator.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.istream.generator.model.ColumnInfo;
import com.istream.generator.model.GenRequest;
import com.istream.generator.model.TableInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 代码生成服务
 *
 * @author isteam
 * @since 2026-08-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeneratorService {

    private final DataSource dataSource;

    /** BaseEntity 中已定义的字段，Entity 模板中跳过 */
    private static final Set<String> BASE_ENTITY_FIELDS = new HashSet<>(Arrays.asList(
            "id", "create_by", "create_time", "update_by", "update_time", "del_flag", "remark"
    ));

    private static final Map<String, String> MYSQL_TYPE_TO_JAVA = new HashMap<>();

    static {
        // 整数类型
        MYSQL_TYPE_TO_JAVA.put("bigint", "Long");
        MYSQL_TYPE_TO_JAVA.put("int", "Integer");
        MYSQL_TYPE_TO_JAVA.put("tinyint", "Integer");
        MYSQL_TYPE_TO_JAVA.put("smallint", "Integer");
        MYSQL_TYPE_TO_JAVA.put("mediumint", "Integer");
        MYSQL_TYPE_TO_JAVA.put("year", "Integer");

        // 浮点数 / 定点数
        MYSQL_TYPE_TO_JAVA.put("decimal", "BigDecimal");
        MYSQL_TYPE_TO_JAVA.put("numeric", "BigDecimal");
        MYSQL_TYPE_TO_JAVA.put("double", "Double");
        MYSQL_TYPE_TO_JAVA.put("float", "Float");

        // 字符串类型
        MYSQL_TYPE_TO_JAVA.put("varchar", "String");
        MYSQL_TYPE_TO_JAVA.put("char", "String");
        MYSQL_TYPE_TO_JAVA.put("text", "String");
        MYSQL_TYPE_TO_JAVA.put("longtext", "String");
        MYSQL_TYPE_TO_JAVA.put("mediumtext", "String");
        MYSQL_TYPE_TO_JAVA.put("tinytext", "String");
        MYSQL_TYPE_TO_JAVA.put("enum", "String");
        MYSQL_TYPE_TO_JAVA.put("set", "String");

        // 日期时间类型
        MYSQL_TYPE_TO_JAVA.put("datetime", "LocalDateTime");
        MYSQL_TYPE_TO_JAVA.put("timestamp", "LocalDateTime");
        MYSQL_TYPE_TO_JAVA.put("date", "LocalDate");
        MYSQL_TYPE_TO_JAVA.put("time", "LocalTime");

        // 二进制类型
        MYSQL_TYPE_TO_JAVA.put("blob", "byte[]");
        MYSQL_TYPE_TO_JAVA.put("mediumblob", "byte[]");
        MYSQL_TYPE_TO_JAVA.put("longblob", "byte[]");
        MYSQL_TYPE_TO_JAVA.put("tinyblob", "byte[]");
        MYSQL_TYPE_TO_JAVA.put("binary", "byte[]");
        MYSQL_TYPE_TO_JAVA.put("varbinary", "byte[]");

        // 其他类型
        MYSQL_TYPE_TO_JAVA.put("bit", "Boolean");
        MYSQL_TYPE_TO_JAVA.put("json", "String");

        // 空间类型（统一映射为 String，由业务层自行处理）
        MYSQL_TYPE_TO_JAVA.put("geometry", "String");
        MYSQL_TYPE_TO_JAVA.put("point", "String");
        MYSQL_TYPE_TO_JAVA.put("linestring", "String");
        MYSQL_TYPE_TO_JAVA.put("polygon", "String");
        MYSQL_TYPE_TO_JAVA.put("multipoint", "String");
        MYSQL_TYPE_TO_JAVA.put("multilinestring", "String");
        MYSQL_TYPE_TO_JAVA.put("multipolygon", "String");
        MYSQL_TYPE_TO_JAVA.put("geometrycollection", "String");
    }

    private static final Set<String> INDEXABLE_TYPES = new HashSet<>(Arrays.asList(
            "bigint", "int", "tinyint", "smallint", "mediumint",
            "varchar", "char", "datetime", "date", "timestamp",
            "decimal", "numeric"
    ));

    // ==================== 公开方法 ====================

    /**
     * 查询所有表信息
     */
    public List<TableInfo> listTables() {
        List<TableInfo> tables = new ArrayList<>();
        String sql = """
                SELECT TABLE_NAME, TABLE_COMMENT, CREATE_TIME
                FROM information_schema.TABLES
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_TYPE = 'BASE TABLE'
                ORDER BY CREATE_TIME DESC
                """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                TableInfo table = TableInfo.builder()
                        .tableName(rs.getString("TABLE_NAME"))
                        .tableComment(rs.getString("TABLE_COMMENT"))
                        .className(tableNameToClassName(rs.getString("TABLE_NAME")))
                        .createTime(rs.getString("CREATE_TIME"))
                        .build();
                tables.add(table);
            }
        } catch (Exception e) {
            log.error("查询表信息失败", e);
            throw new RuntimeException("查询表信息失败: " + e.getMessage());
        }
        return tables;
    }

    /**
     * 查询指定表的列信息
     */
    public List<ColumnInfo> listColumns(String tableName) {
        List<ColumnInfo> columns = new ArrayList<>();
        String sql = """
                SELECT COLUMN_NAME, COLUMN_COMMENT, DATA_TYPE, COLUMN_TYPE,
                       IS_NULLABLE, COLUMN_KEY
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                ORDER BY ORDINAL_POSITION
                """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String columnName = rs.getString("COLUMN_NAME");
                    String dataType = rs.getString("DATA_TYPE").toLowerCase();
                    String columnType = rs.getString("COLUMN_TYPE");

                    ColumnInfo col = ColumnInfo.builder()
                            .columnName(columnName)
                            .columnComment(defaultIfEmpty(rs.getString("COLUMN_COMMENT"), columnName))
                            .javaType(mapJavaType(dataType, columnType))
                            .javaField(columnNameToField(columnName))
                            .isPk("PRI".equals(rs.getString("COLUMN_KEY")))
                            .isRequired("NO".equals(rs.getString("IS_NULLABLE")))
                            .isBaseField(BASE_ENTITY_FIELDS.contains(columnName.toLowerCase()))
                            .sqlType(columnType.toUpperCase())
                            .isIndexable(INDEXABLE_TYPES.contains(dataType))
                            .build();
                    columns.add(col);
                }
            }
        } catch (Exception e) {
            log.error("查询列信息失败: {}", tableName, e);
            throw new RuntimeException("查询列信息失败: " + e.getMessage());
        }
        return columns;
    }

    /**
     * 预览生成代码
     *
     * @return Map<模板名, 生成内容>
     */
    public Map<String, String> preview(String tableName, GenRequest request) {
        TableInfo tableInfo = buildTableInfo(tableName, request);
        return renderAll(tableInfo, request);
    }

    /**
     * 批量预览
     */
    public Map<String, Map<String, String>> batchPreview(GenRequest request) {
        Map<String, Map<String, String>> result = new LinkedHashMap<>();
        for (String tableName : request.getTableNames()) {
            result.put(tableName, preview(tableName, request));
        }
        return result;
    }

    // ==================== 私有方法 ====================

    /**
     * 构建表元数据
     */
    private TableInfo buildTableInfo(String tableName, GenRequest request) {
        List<ColumnInfo> columns = listColumns(tableName);
        String tableComment = getTableComment(tableName);
        return TableInfo.builder()
                .tableName(tableName)
                .tableComment(tableComment)
                .className(tableNameToClassName(tableName))
                .columns(columns)
                .build();
    }

    /**
     * 获取表注释
     */
    private String getTableComment(String tableName) {
        String sql = """
                SELECT TABLE_COMMENT
                FROM information_schema.TABLES
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return defaultIfEmpty(rs.getString("TABLE_COMMENT"), tableName);
                }
            }
        } catch (Exception e) {
            log.error("获取表注释失败: {}", tableName, e);
        }
        return tableName;
    }

    /**
     * 渲染所有模板
     */
    private Map<String, String> renderAll(TableInfo tableInfo, GenRequest request) {
        VelocityEngine engine = createVelocityEngine();
        VelocityContext context = buildContext(tableInfo, request);

        Map<String, String> result = new LinkedHashMap<>();
        result.put("entity.java", renderTemplate(engine, context, "templates/generator/entity.java.vm"));
        result.put("mapper.java", renderTemplate(engine, context, "templates/generator/mapper.java.vm"));
        result.put("service.java", renderTemplate(engine, context, "templates/generator/service.java.vm"));
        result.put("serviceImpl.java", renderTemplate(engine, context, "templates/generator/serviceImpl.java.vm"));
        result.put("controller.java", renderTemplate(engine, context, "templates/generator/controller.java.vm"));
        if (request.isGenMigration()) {
            result.put("migration.sql", renderTemplate(engine, context, "templates/generator/migration.sql.vm"));
        }
        return result;
    }

    /**
     * 渲染单个模板
     */
    private String renderTemplate(VelocityEngine engine, VelocityContext context, String templatePath) {
        try (StringWriter writer = new StringWriter()) {
            engine.mergeTemplate(templatePath, "UTF-8", context, writer);
            return writer.toString();
        } catch (Exception e) {
            log.error("模板渲染失败: {}", templatePath, e);
            return "<!-- 渲染失败: " + e.getMessage() + " -->";
        }
    }

    /**
     * 创建 Velocity 引擎
     */
    private VelocityEngine createVelocityEngine() {
        Properties props = new Properties();
        props.setProperty("resource.loader.file.class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        props.setProperty("resource.default_encoding", "UTF-8");
        props.setProperty("output.encoding", "UTF-8");
        VelocityEngine engine = new VelocityEngine();
        engine.init(props);
        return engine;
    }

    /**
     * 构建 Velocity 上下文
     */
    private VelocityContext buildContext(TableInfo tableInfo, GenRequest request) {
        VelocityContext context = new VelocityContext();

        String className = tableInfo.getClassName();
        context.put("packageName", request.getPackageName());
        context.put("moduleName", request.getModuleName());
        context.put("author", request.getAuthor());
        context.put("date", DateUtil.format(new Date(), "yyyy-MM-dd"));
        context.put("tableComment", tableInfo.getTableComment());
        context.put("className", className);
        context.put("classVar", StrUtil.lowerFirst(className));
        context.put("tableName", tableInfo.getTableName());
        context.put("pathName", tableNameToPath(tableInfo.getTableName()));
        context.put("permissionPrefix", request.getModuleName() + ":" + tableNameToPath(tableInfo.getTableName()));
        context.put("controllerPackage", request.getControllerPackage());
        context.put("columns", tableInfo.getColumns());

        // 计算需要的导入包
        Set<String> importPackages = new HashSet<>();
        for (ColumnInfo col : tableInfo.getColumns()) {
            if (col.isBaseField()) {
                continue;
            }
            if ("LocalDateTime".equals(col.getJavaType())) {
                importPackages.add("java.time.LocalDateTime");
            } else if ("LocalDate".equals(col.getJavaType())) {
                importPackages.add("java.time.LocalDate");
            } else if ("LocalTime".equals(col.getJavaType())) {
                importPackages.add("java.time.LocalTime");
            } else if ("BigDecimal".equals(col.getJavaType())) {
                importPackages.add("java.math.BigDecimal");
            }
        }
        context.put("importPackages", importPackages);

        // 索引列
        List<String> indexColumns = tableInfo.getColumns().stream()
                .filter(c -> !c.isBaseField() && c.isIndexable())
                .map(ColumnInfo::getColumnName)
                .collect(Collectors.toList());
        context.put("indexColumns", indexColumns);

        return context;
    }

    // ==================== 工具方法 ====================

    /**
     * 表名转类名：sys_user → SysUser
     */
    static String tableNameToClassName(String tableName) {
        return StrUtil.upperFirst(StrUtil.toCamelCase(tableName.toLowerCase()));
    }

    /**
     * 列名转字段名：create_time → createTime
     */
    static String columnNameToField(String columnName) {
        return StrUtil.toCamelCase(columnName.toLowerCase());
    }

    /**
     * 表名转 URL 路径：sys_user → user（去掉前缀 sys_/t_/biz_）
     */
    static String tableNameToPath(String tableName) {
        String name = tableName.toLowerCase();
        for (String prefix : new String[]{"sys_", "t_", "biz_", "tb_"}) {
            if (name.startsWith(prefix)) {
                name = name.substring(prefix.length());
                break;
            }
        }
        return StrUtil.toCamelCase(name);
    }

    /**
     * MySQL 类型映射到 Java 类型
     *
     * @param dataType   DATA_TYPE 字段值（如 tinyint、varchar）
     * @param columnType COLUMN_TYPE 字段值（如 tinyint(1)、varchar(255)）
     */
    static String mapJavaType(String dataType, String columnType) {
        String colType = columnType != null ? columnType.toLowerCase() : "";

        if ("tinyint".equals(dataType) && "tinyint(1)".equals(colType)) {
            return "Boolean";
        }

        int parenIdx = dataType.indexOf('(');
        if (parenIdx > 0) {
            dataType = dataType.substring(0, parenIdx);
        }
        return MYSQL_TYPE_TO_JAVA.getOrDefault(dataType, "String");
    }

    static String defaultIfEmpty(String str, String defaultStr) {
        return StrUtil.isBlank(str) ? defaultStr : str;
    }
}