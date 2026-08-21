package com.istream.generator.service;

import com.istream.generator.model.ColumnInfo;
import com.istream.generator.model.GenRequest;
import com.istream.generator.model.TableInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@DisplayName("GeneratorService 单元测试")
class GeneratorServiceTest {

    // ==================== 静态工具方法测试（无需 mock） ====================

    @Nested
    @DisplayName("表名转类名")
    class TableNameToClassName {

        @Test
        @DisplayName("sys_user → SysUser")
        void standard() {
            assertThat(GeneratorService.tableNameToClassName("sys_user")).isEqualTo("SysUser");
        }

        @Test
        @DisplayName("单表名 config → Config")
        void singleWord() {
            assertThat(GeneratorService.tableNameToClassName("config")).isEqualTo("Config");
        }

        @Test
        @DisplayName("多下划线 sys_user_role → SysUserRole")
        void multiUnderscore() {
            assertThat(GeneratorService.tableNameToClassName("sys_user_role")).isEqualTo("SysUserRole");
        }

        @Test
        @DisplayName("大小写混合 SYS_USER → SysUser")
        void mixedCase() {
            assertThat(GeneratorService.tableNameToClassName("SYS_USER")).isEqualTo("SysUser");
        }
    }

    @Nested
    @DisplayName("列名转字段名")
    class ColumnNameToField {

        @Test
        @DisplayName("create_time → createTime")
        void standard() {
            assertThat(GeneratorService.columnNameToField("create_time")).isEqualTo("createTime");
        }

        @Test
        @DisplayName("单列名 username → username")
        void singleWord() {
            assertThat(GeneratorService.columnNameToField("username")).isEqualTo("username");
        }
    }

    @Nested
    @DisplayName("表名转路径")
    class TableNameToPath {

        @Test
        @DisplayName("sys_user → user")
        void sysPrefix() {
            assertThat(GeneratorService.tableNameToPath("sys_user")).isEqualTo("user");
        }

        @Test
        @DisplayName("biz_order → order")
        void bizPrefix() {
            assertThat(GeneratorService.tableNameToPath("biz_order")).isEqualTo("order");
        }

        @Test
        @DisplayName("t_category → category")
        void tPrefix() {
            assertThat(GeneratorService.tableNameToPath("t_category")).isEqualTo("category");
        }

        @Test
        @DisplayName("tb_order → order")
        void tbPrefix() {
            assertThat(GeneratorService.tableNameToPath("tb_order")).isEqualTo("order");
        }

        @Test
        @DisplayName("无前缀 product → product")
        void noPrefix() {
            assertThat(GeneratorService.tableNameToPath("product")).isEqualTo("product");
        }
    }

    @Nested
    @DisplayName("MySQL 类型映射")
    class MapJavaType {

        @Test
        @DisplayName("bigint → Long")
        void bigint() {
            assertThat(GeneratorService.mapJavaType("bigint", null)).isEqualTo("Long");
        }

        @Test
        @DisplayName("varchar(255) → String")
        void varchar() {
            assertThat(GeneratorService.mapJavaType("varchar", "varchar(255)")).isEqualTo("String");
        }

        @Test
        @DisplayName("datetime → LocalDateTime")
        void datetime() {
            assertThat(GeneratorService.mapJavaType("datetime", null)).isEqualTo("LocalDateTime");
        }

        @Test
        @DisplayName("decimal(10,2) → BigDecimal")
        void decimal() {
            assertThat(GeneratorService.mapJavaType("decimal", "decimal(10,2)")).isEqualTo("BigDecimal");
        }

        @Test
        @DisplayName("tinyint(1) → Boolean")
        void tinyint1() {
            assertThat(GeneratorService.mapJavaType("tinyint", "tinyint(1)")).isEqualTo("Boolean");
        }

        @Test
        @DisplayName("tinyint(4) → Integer")
        void tinyint4() {
            assertThat(GeneratorService.mapJavaType("tinyint", "tinyint(4)")).isEqualTo("Integer");
        }

        @Test
        @DisplayName("date → LocalDate")
        void date() {
            assertThat(GeneratorService.mapJavaType("date", null)).isEqualTo("LocalDate");
        }

        @Test
        @DisplayName("text → String")
        void text() {
            assertThat(GeneratorService.mapJavaType("text", null)).isEqualTo("String");
        }

        @Test
        @DisplayName("float → Float")
        void floatType() {
            assertThat(GeneratorService.mapJavaType("float", null)).isEqualTo("Float");
        }

        @Test
        @DisplayName("double → Double")
        void doubleType() {
            assertThat(GeneratorService.mapJavaType("double", null)).isEqualTo("Double");
        }

        @Test
        @DisplayName("bit → Boolean")
        void bit() {
            assertThat(GeneratorService.mapJavaType("bit", null)).isEqualTo("Boolean");
        }

        @Test
        @DisplayName("json → String")
        void json() {
            assertThat(GeneratorService.mapJavaType("json", null)).isEqualTo("String");
        }

        @Test
        @DisplayName("blob → byte[]")
        void blob() {
            assertThat(GeneratorService.mapJavaType("blob", null)).isEqualTo("byte[]");
        }

        @Test
        @DisplayName("year → Integer")
        void year() {
            assertThat(GeneratorService.mapJavaType("year", null)).isEqualTo("Integer");
        }

        @Test
        @DisplayName("enum → String")
        void enumType() {
            assertThat(GeneratorService.mapJavaType("enum", "enum('a','b')")).isEqualTo("String");
        }

        @Test
        @DisplayName("未知类型 → String")
        void unknown() {
            assertThat(GeneratorService.mapJavaType("unknown_type", null)).isEqualTo("String");
        }
    }

    @Nested
    @DisplayName("默认值处理")
    class DefaultIfEmpty {

        @Test
        @DisplayName("null → 默认值")
        void nullValue() {
            assertThat(GeneratorService.defaultIfEmpty(null, "default")).isEqualTo("default");
        }

        @Test
        @DisplayName("空白 → 默认值")
        void blankValue() {
            assertThat(GeneratorService.defaultIfEmpty("   ", "default")).isEqualTo("default");
        }

        @Test
        @DisplayName("有值 → 原值")
        void hasValue() {
            assertThat(GeneratorService.defaultIfEmpty("hello", "default")).isEqualTo("hello");
        }
    }

    // ==================== GenRequest 默认值 ====================

    @Nested
    @DisplayName("GenRequest 默认值")
    class GenRequestDefaults {

        @Test
        @DisplayName("author = isteam")
        void author() {
            assertThat(new GenRequest().getAuthor()).isEqualTo("isteam");
        }

        @Test
        @DisplayName("moduleName = system")
        void moduleName() {
            assertThat(new GenRequest().getModuleName()).isEqualTo("system");
        }

        @Test
        @DisplayName("genMigration = true")
        void genMigration() {
            assertThat(new GenRequest().isGenMigration()).isTrue();
        }
    }

    // ==================== 数据库依赖方法（需要 mock） ====================

    @Nested
    @DisplayName("数据库操作")
    @ExtendWith(MockitoExtension.class)
    @MockitoSettings(strictness = Strictness.LENIENT)
    class DatabaseOperations {

        @Mock
        private DataSource dataSource;

        @Mock
        private Connection connection;

        @Mock
        private PreparedStatement preparedStatement;

        @Mock
        private ResultSet resultSet;

        private GeneratorService generatorService;

        @BeforeEach
        void setUp() throws Exception {
            reset(dataSource, connection, preparedStatement, resultSet);
            generatorService = new GeneratorService(dataSource);
            when(dataSource.getConnection()).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
        }

        @Nested
        @DisplayName("listTables")
        class ListTables {

            @Test
            @DisplayName("正常返回表列表")
            void normal() throws Exception {
                when(resultSet.next()).thenReturn(true, true, false);
                when(resultSet.getString("TABLE_NAME")).thenReturn("sys_user", "sys_user", "sys_role", "sys_role");
                when(resultSet.getString("TABLE_COMMENT")).thenReturn("用户表", "角色表");
                when(resultSet.getString("CREATE_TIME")).thenReturn("2026-01-01", "2026-01-02");

                List<TableInfo> tables = generatorService.listTables();

                assertThat(tables).hasSize(2);
                assertThat(tables.get(0).getTableName()).isEqualTo("sys_user");
                assertThat(tables.get(0).getTableComment()).isEqualTo("用户表");
                assertThat(tables.get(0).getClassName()).isEqualTo("SysUser");
                assertThat(tables.get(1).getClassName()).isEqualTo("SysRole");
            }

            @Test
            @DisplayName("空数据库返回空列表")
            void empty() throws Exception {
                when(resultSet.next()).thenReturn(false);

                List<TableInfo> tables = generatorService.listTables();

                assertThat(tables).isEmpty();
            }

            @Test
            @DisplayName("连接失败抛 RuntimeException")
            void exception() throws Exception {
                when(dataSource.getConnection()).thenThrow(new RuntimeException("Connection failed"));

                assertThatThrownBy(() -> generatorService.listTables())
                        .isInstanceOf(RuntimeException.class)
                        .hasMessageContaining("查询表信息失败");
            }
        }

        @Nested
        @DisplayName("listColumns")
        class ListColumns {

            @Test
            @DisplayName("正常返回含主键列")
            void withPk() throws Exception {
                when(resultSet.next()).thenReturn(true, true, false);
                when(resultSet.getString("COLUMN_NAME")).thenReturn("id", "username");
                when(resultSet.getString("COLUMN_COMMENT")).thenReturn("主键ID", "用户名");
                when(resultSet.getString("DATA_TYPE")).thenReturn("bigint", "varchar");
                when(resultSet.getString("COLUMN_TYPE")).thenReturn("bigint", "varchar(64)");
                when(resultSet.getString("IS_NULLABLE")).thenReturn("NO", "NO");
                when(resultSet.getString("COLUMN_KEY")).thenReturn("PRI", "");

                List<ColumnInfo> columns = generatorService.listColumns("sys_user");

                assertThat(columns).hasSize(2);
                ColumnInfo pk = columns.get(0);
                assertThat(pk.getColumnName()).isEqualTo("id");
                assertThat(pk.isPk()).isTrue();
                assertThat(pk.isBaseField()).isTrue();
                assertThat(pk.getJavaType()).isEqualTo("Long");

                ColumnInfo username = columns.get(1);
                assertThat(username.getColumnName()).isEqualTo("username");
                assertThat(username.getJavaField()).isEqualTo("username");
                assertThat(username.isRequired()).isTrue();
                assertThat(username.isBaseField()).isFalse();
            }

            @Test
            @DisplayName("可为空列 isRequired = false")
            void nullable() throws Exception {
                when(resultSet.next()).thenReturn(true, false);
                when(resultSet.getString("COLUMN_NAME")).thenReturn("remark");
                when(resultSet.getString("COLUMN_COMMENT")).thenReturn("备注");
                when(resultSet.getString("DATA_TYPE")).thenReturn("varchar");
                when(resultSet.getString("COLUMN_TYPE")).thenReturn("varchar(500)");
                when(resultSet.getString("IS_NULLABLE")).thenReturn("YES");
                when(resultSet.getString("COLUMN_KEY")).thenReturn("");

                List<ColumnInfo> columns = generatorService.listColumns("sys_user");

                assertThat(columns.get(0).isRequired()).isFalse();
                assertThat(columns.get(0).isBaseField()).isTrue();
            }

            @Test
            @DisplayName("空注释回退为列名")
            void emptyComment() throws Exception {
                when(resultSet.next()).thenReturn(true, false);
                when(resultSet.getString("COLUMN_NAME")).thenReturn("status");
                when(resultSet.getString("COLUMN_COMMENT")).thenReturn("");
                when(resultSet.getString("DATA_TYPE")).thenReturn("int");
                when(resultSet.getString("COLUMN_TYPE")).thenReturn("int");
                when(resultSet.getString("IS_NULLABLE")).thenReturn("NO");
                when(resultSet.getString("COLUMN_KEY")).thenReturn("");

                List<ColumnInfo> columns = generatorService.listColumns("sys_user");

                assertThat(columns.get(0).getColumnComment()).isEqualTo("status");
            }

            @Test
            @DisplayName("异常时抛 RuntimeException")
            void exception() throws Exception {
                when(dataSource.getConnection()).thenThrow(new RuntimeException("Connection failed"));

                assertThatThrownBy(() -> generatorService.listColumns("sys_user"))
                        .isInstanceOf(RuntimeException.class)
                        .hasMessageContaining("查询列信息失败");
            }
        }

        @Nested
        @DisplayName("preview 模板渲染")
        class Preview {

            @BeforeEach
            void setUpColumns() throws Exception {
                when(resultSet.next()).thenReturn(false, true, false);
                when(resultSet.getString("TABLE_COMMENT")).thenReturn("用户表");
            }

            @Test
            @DisplayName("包含所有模板文件")
            void allTemplates() {
                GenRequest request = new GenRequest();
                request.setModuleName("system");
                request.setPackageName("com.istream.system");
                request.setControllerPackage("com.istream.admin.controller");
                request.setGenMigration(true);

                Map<String, String> result = generatorService.preview("sys_user", request);

                assertThat(result).containsKeys("entity.java", "mapper.java", "service.java",
                        "serviceImpl.java", "controller.java", "migration.sql");
            }

            @Test
            @DisplayName("genMigration=false 不生成 SQL")
            void noMigration() {
                GenRequest request = new GenRequest();
                request.setModuleName("system");
                request.setGenMigration(false);

                Map<String, String> result = generatorService.preview("sys_user", request);

                assertThat(result).doesNotContainKey("migration.sql");
                assertThat(result).containsKeys("entity.java", "mapper.java", "service.java",
                        "serviceImpl.java", "controller.java");
            }

            @Test
            @DisplayName("Entity 包含表注释和类名")
            void entityContent() {
                GenRequest request = new GenRequest();
                request.setModuleName("system");
                request.setGenMigration(false);

                Map<String, String> result = generatorService.preview("sys_user", request);

                assertThat(result.get("entity.java")).contains("用户表");
                assertThat(result.get("entity.java")).contains("class SysUser");
                assertThat(result.get("service.java")).contains("interface SysUserService");
            }
        }

        @Nested
        @DisplayName("batchPreview 批量预览")
        class BatchPreview {

            @BeforeEach
            void setUpColumns() throws Exception {
                when(resultSet.next()).thenReturn(false, true, false, false, true, false);
                when(resultSet.getString("TABLE_COMMENT")).thenReturn("用户表", "角色表");
            }

            @Test
            @DisplayName("多表返回")
            void multipleTables() {
                GenRequest request = new GenRequest();
                request.setTableNames(List.of("sys_user", "sys_role"));
                request.setGenMigration(false);

                Map<String, Map<String, String>> result = generatorService.batchPreview(request);

                assertThat(result).hasSize(2);
                assertThat(result).containsKeys("sys_user", "sys_role");
            }

            @Test
            @DisplayName("空表列表返回空 Map")
            void emptyList() {
                GenRequest request = new GenRequest();
                request.setTableNames(List.of());
                request.setGenMigration(false);

                Map<String, Map<String, String>> result = generatorService.batchPreview(request);

                assertThat(result).isEmpty();
            }
        }
    }
}