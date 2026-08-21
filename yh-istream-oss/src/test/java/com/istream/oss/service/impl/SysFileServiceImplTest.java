package com.istream.oss.service.impl;

import com.istream.common.exception.BusinessException;
import com.istream.oss.entity.SysFile;
import com.istream.oss.mapper.SysFileMapper;
import com.istream.oss.storage.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SysFileServiceImpl 单元测试")
class SysFileServiceImplTest {

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private SysFileMapper sysFileMapper;

    private SysFileServiceImpl sysFileService;

    @BeforeEach
    void setUp() {
        sysFileService = new SysFileServiceImpl(fileStorageService);
        ReflectionTestUtils.setField(sysFileService, "baseMapper", sysFileMapper);
    }

    @Test
    @DisplayName("上传文件 — 成功")
    void upload_Success() {
        MultipartFile file = new MockMultipartFile(
                "file", "test.png", "image/png",
                "fake-image-content".getBytes(StandardCharsets.UTF_8));
        when(fileStorageService.store(any(InputStream.class), anyString(), anyString()))
                .thenReturn("2026/08/14/uuid123.png");
        when(fileStorageService.getAccessUrl(anyString()))
                .thenReturn("/files/2026/08/14/uuid123.png");
        when(sysFileMapper.insert(any(SysFile.class))).thenReturn(1);

        SysFile result = sysFileService.upload(file, "common");

        assertNotNull(result);
        assertEquals("test.png", result.getOriginalName());
        assertEquals("png", result.getFileExt());
        assertEquals("image/png", result.getMimeType());
        verify(sysFileMapper).insert(any(SysFile.class));
    }

    @Test
    @DisplayName("上传文件 — 空文件")
    void upload_EmptyFile() {
        MultipartFile file = new MockMultipartFile(
                "file", "empty.txt", "text/plain", new byte[0]);

        assertThrows(BusinessException.class, () -> sysFileService.upload(file, "common"));
    }

    @Test
    @DisplayName("上传文件 — 超大文件")
    void upload_FileTooLarge() {
        byte[] largeContent = new byte[11 * 1024 * 1024];
        MultipartFile file = new MockMultipartFile(
                "file", "large.png", "image/png", largeContent);

        assertThrows(BusinessException.class, () -> sysFileService.upload(file, "common"));
    }

    @Test
    @DisplayName("上传文件 — 不支持的类型")
    void upload_InvalidExtension() {
        MultipartFile file = new MockMultipartFile(
                "file", "script.exe", "application/octet-stream",
                "malicious".getBytes(StandardCharsets.UTF_8));

        assertThrows(BusinessException.class, () -> sysFileService.upload(file, "common"));
    }

    @Test
    @DisplayName("获取文件流 — 成功")
    void getFileStream_Success() {
        SysFile sysFile = new SysFile();
        sysFile.setId(1L);
        sysFile.setFilePath("2026/08/14/uuid123.png");
        when(sysFileMapper.selectById(anyLong())).thenReturn(sysFile);
        when(fileStorageService.load(anyString()))
                .thenReturn(new ByteArrayInputStream("content".getBytes(StandardCharsets.UTF_8)));

        InputStream result = sysFileService.getFileStream(1L);

        assertNotNull(result);
    }

    @Test
    @DisplayName("获取文件流 — 文件不存在")
    void getFileStream_NotFound() {
        when(sysFileMapper.selectById(anyLong())).thenReturn(null);

        assertThrows(BusinessException.class, () -> sysFileService.getFileStream(999L));
    }

    @Test
    @DisplayName("删除文件 — 成功")
    void deleteFile_Success() {
        SysFile sysFile = new SysFile();
        sysFile.setId(1L);
        sysFile.setFilePath("2026/08/14/uuid123.png");
        when(sysFileMapper.selectById(anyLong())).thenReturn(sysFile);
        when(sysFileMapper.deleteById(anyLong())).thenReturn(1);

        assertDoesNotThrow(() -> sysFileService.deleteFile(1L));
        verify(sysFileMapper).deleteById(anyLong());
        verify(fileStorageService).delete(anyString());
    }

    @Test
    @DisplayName("删除文件 — 文件不存在")
    void deleteFile_NotFound() {
        when(sysFileMapper.selectById(anyLong())).thenReturn(null);

        assertThrows(BusinessException.class, () -> sysFileService.deleteFile(999L));
    }

    @Test
    @DisplayName("删除文件 — 磁盘删除失败不抛异常")
    void deleteFile_DiskDeleteFailed() {
        SysFile sysFile = new SysFile();
        sysFile.setId(1L);
        sysFile.setFilePath("2026/08/14/uuid123.png");
        when(sysFileMapper.selectById(anyLong())).thenReturn(sysFile);
        when(sysFileMapper.deleteById(anyLong())).thenReturn(1);
        doThrow(new RuntimeException("磁盘故障")).when(fileStorageService).delete(anyString());

        assertDoesNotThrow(() -> sysFileService.deleteFile(1L));
        verify(sysFileMapper).deleteById(anyLong());
    }
}