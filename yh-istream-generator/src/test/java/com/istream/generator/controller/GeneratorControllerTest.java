package com.istream.generator.controller;

import com.istream.generator.model.ColumnInfo;
import com.istream.generator.model.GenRequest;
import com.istream.generator.model.TableInfo;
import com.istream.generator.service.GeneratorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("GeneratorController 接口测试")
class GeneratorControllerTest {

    @Mock
    private GeneratorService generatorService;

    @InjectMocks
    private GeneratorController generatorController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(generatorController).build();
    }

    private TableInfo createTableInfo(String name, String comment) {
        return TableInfo.builder()
                .tableName(name)
                .tableComment(comment)
                .className(name)
                .columns(List.of())
                .build();
    }

    private ColumnInfo createColumnInfo(String name, String javaType, String comment) {
        ColumnInfo col = new ColumnInfo();
        col.setColumnName(name);
        col.setJavaType(javaType);
        col.setColumnComment(comment);
        return col;
    }

    @Nested
    @DisplayName("查询所有表")
    class ListTablesTests {

        @Test
        @DisplayName("返回表列表")
        void listTables_Success() throws Exception {
            when(generatorService.listTables()).thenReturn(List.of(
                    createTableInfo("sys_user", "用户表"),
                    createTableInfo("sys_role", "角色表")));

            mockMvc.perform(get("/generator/tables"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data", hasSize(2)))
                    .andExpect(jsonPath("$.data[0].tableName").value("sys_user"))
                    .andExpect(jsonPath("$.data[1].tableName").value("sys_role"));
        }

        @Test
        @DisplayName("数据库无表，返回空列表")
        void listTables_Empty() throws Exception {
            when(generatorService.listTables()).thenReturn(List.of());

            mockMvc.perform(get("/generator/tables"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("查询表列信息")
    class ListColumnsTests {

        @Test
        @DisplayName("返回列信息列表")
        void listColumns_Success() throws Exception {
            when(generatorService.listColumns("sys_user")).thenReturn(List.of(
                    createColumnInfo("id", "Long", "主键ID"),
                    createColumnInfo("username", "String", "用户名"),
                    createColumnInfo("email", "String", "邮箱")));

            mockMvc.perform(get("/generator/columns/sys_user"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data", hasSize(3)))
                    .andExpect(jsonPath("$.data[0].columnName").value("id"))
                    .andExpect(jsonPath("$.data[1].javaType").value("String"))
                    .andExpect(jsonPath("$.data[2].columnComment").value("邮箱"));
        }

        @Test
        @DisplayName("表不存在，返回空列表")
        void listColumns_EmptyTable() throws Exception {
            when(generatorService.listColumns("unknown_table")).thenReturn(List.of());

            mockMvc.perform(get("/generator/columns/unknown_table"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("预览生成代码")
    class PreviewTests {

        @Test
        @DisplayName("单表预览成功")
        void preview_Success() throws Exception {
            Map<String, String> codeMap = new LinkedHashMap<>();
            codeMap.put("entity.java", "package com.istream.system.entity;\n\npublic class SysUser {}");
            codeMap.put("mapper.java", "package com.istream.system.mapper;\n\npublic interface SysUserMapper {}");
            codeMap.put("service.java", "package com.istream.system.service;\n\npublic interface SysUserService {}");
            codeMap.put("serviceImpl.java", "package com.istream.system.service.impl;\n\npublic class SysUserServiceImpl {}");
            codeMap.put("controller.java", "package com.istream.admin.controller;\n\npublic class SysUserController {}");

            GenRequest request = new GenRequest();
            request.setModuleName("system");

            when(generatorService.preview(eq("sys_user"), any(GenRequest.class))).thenReturn(codeMap);

            mockMvc.perform(post("/generator/preview/sys_user")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"moduleName\":\"system\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data['entity.java']").exists())
                    .andExpect(jsonPath("$.data['mapper.java']").exists())
                    .andExpect(jsonPath("$.data['service.java']").exists())
                    .andExpect(jsonPath("$.data['serviceImpl.java']").exists())
                    .andExpect(jsonPath("$.data['controller.java']").exists());
        }

        @Test
        @DisplayName("预览包含迁移 SQL")
        void preview_WithMigration() throws Exception {
            Map<String, String> codeMap = new LinkedHashMap<>();
            codeMap.put("entity.java", "// entity");
            codeMap.put("migration.sql", "-- migration");

            GenRequest request = new GenRequest();
            request.setModuleName("system");
            request.setGenMigration(true);

            when(generatorService.preview(eq("sys_config"), any(GenRequest.class))).thenReturn(codeMap);

            mockMvc.perform(post("/generator/preview/sys_config")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"moduleName\":\"system\",\"genMigration\":true}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data['migration.sql']").exists());
        }

        @Test
        @DisplayName("批量预览多表")
        void batchPreview_Success() throws Exception {
            Map<String, String> userCode = new LinkedHashMap<>();
            userCode.put("entity.java", "// SysUser");

            Map<String, String> roleCode = new LinkedHashMap<>();
            roleCode.put("entity.java", "// SysRole");

            Map<String, Map<String, String>> batchResult = new LinkedHashMap<>();
            batchResult.put("sys_user", userCode);
            batchResult.put("sys_role", roleCode);

            GenRequest request = new GenRequest();
            request.setTableNames(List.of("sys_user", "sys_role"));

            when(generatorService.batchPreview(any(GenRequest.class))).thenReturn(batchResult);

            mockMvc.perform(post("/generator/batch-preview")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"tableNames\":[\"sys_user\",\"sys_role\"]}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data['sys_user']").exists())
                    .andExpect(jsonPath("$.data['sys_role']").exists());
        }
    }

    @Nested
    @DisplayName("下载生成代码")
    class DownloadTests {

        @Test
        @DisplayName("单表下载，返回 ZIP 二进制流")
        void download_Success() throws Exception {
            Map<String, String> codeMap = new LinkedHashMap<>();
            codeMap.put("entity.java", "package com.istream.system.entity;\npublic class SysUser {}");

            when(generatorService.preview(eq("sys_user"), any(GenRequest.class))).thenReturn(codeMap);

            mockMvc.perform(post("/generator/download/sys_user")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"moduleName\":\"system\"}"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/octet-stream"))
                    .andExpect(header().string("Content-Disposition",
                            org.hamcrest.Matchers.containsString("sys_user.zip")));

            verify(generatorService).preview(eq("sys_user"), any(GenRequest.class));
        }

        @Test
        @DisplayName("批量下载，返回 ZIP 二进制流")
        void batchDownload_Success() throws Exception {
            Map<String, String> userCode = new LinkedHashMap<>();
            userCode.put("entity.java", "// SysUser");

            Map<String, Map<String, String>> batchResult = new LinkedHashMap<>();
            batchResult.put("sys_user", userCode);

            when(generatorService.batchPreview(any(GenRequest.class))).thenReturn(batchResult);

            mockMvc.perform(post("/generator/batch-download")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"tableNames\":[\"sys_user\"]}"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/octet-stream"))
                    .andExpect(header().string("Content-Disposition",
                            org.hamcrest.Matchers.containsString("generator-output.zip")));

            verify(generatorService).batchPreview(any(GenRequest.class));
        }
    }
}