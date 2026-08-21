package com.istream.admin.controller;

import com.istream.oss.entity.SysFile;
import com.istream.oss.service.SysFileService;
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
import org.springframework.mock.web.MockMultipartFile;
import com.istream.framework.security.GlobalExceptionHandler;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SysFileController 接口测试")
class SysFileControllerTest {

    @Mock
    private SysFileService sysFileService;

    @InjectMocks
    private SysFileController sysFileController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(sysFileController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        ReflectionTestUtils.setField(sysFileController, "maxFileSize", "10MB");
    }

    private SysFile createSysFile(Long id, String originalName) {
        SysFile file = new SysFile();
        file.setId(id);
        file.setFileName("uuid_" + originalName);
        file.setOriginalName(originalName);
        file.setFilePath("/uploads/uuid_" + originalName);
        file.setFileSize(1024L);
        file.setMimeType("image/png");
        file.setFileExt("png");
        return file;
    }

    @Nested
    @DisplayName("文件上传")
    class UploadTests {

        @Test
        @DisplayName("上传空文件")
        void upload_EmptyFile() throws Exception {
            MockMultipartFile emptyFile = new MockMultipartFile(
                    "file", "test.png", "image/png", new byte[0]);

            mockMvc.perform(multipart("/system/file/upload").file(emptyFile))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(20001));
        }

        @Test
        @DisplayName("上传不支持的文件类型")
        void upload_UnsupportedType() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.exe", "application/x-msdownload",
                    "fake exe content".getBytes(StandardCharsets.UTF_8));

            mockMvc.perform(multipart("/system/file/upload").file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(20001));
        }

        @Test
        @DisplayName("上传合法PNG文件")
        void upload_Success() throws Exception {
            byte[] pngContent = new byte[] {
                    (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
                    0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52
            };
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.png", "image/png", pngContent);

            SysFile savedFile = createSysFile(1L, "test.png");
            when(sysFileService.upload(any(), any())).thenReturn(savedFile);

            mockMvc.perform(multipart("/system/file/upload").file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.originalName").value("test.png"));
        }
    }

    @Nested
    @DisplayName("文件下载")
    class DownloadTests {

        @Test
        @DisplayName("文件不存在")
        void download_NotFound() throws Exception {
            when(sysFileService.getById(999L)).thenReturn(null);

            mockMvc.perform(get("/system/file/download/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(20003));
        }
    }

    @Nested
    @DisplayName("文件列表")
    class ListTests {

        @Test
        @DisplayName("正常查询")
        void list_Success() throws Exception {
            mockMvc.perform(get("/system/file/list"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").exists());
        }
    }

    @Nested
    @DisplayName("文件删除")
    class DeleteTests {

        @Test
        @DisplayName("正常删除")
        void delete_Success() throws Exception {
            doNothing().when(sysFileService).deleteFile(1L);

            mockMvc.perform(delete("/system/file/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("文件预览")
    class PreviewTests {

        @Test
        @DisplayName("文件不存在")
        void preview_NotFound() throws Exception {
            when(sysFileService.getById(999L)).thenReturn(null);

            mockMvc.perform(get("/system/file/preview/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(20003));
        }
    }
}