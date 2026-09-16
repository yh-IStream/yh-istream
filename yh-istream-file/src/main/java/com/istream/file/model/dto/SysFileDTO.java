package com.istream.file.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.istream.file.enums.StorageType;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文件信息 DTO（用于列表/详情返回）
 *
 * @author istream
 * @since 2026-09-15
 */
@Data
public class SysFileDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    private String fileName;

    private String originalName;

    private String filePath;

    private Long fileSize;

    private String mimeType;

    private String fileExt;

    private StorageType storageType;

    private String storageUrl;

    private String module;

    private LocalDateTime createTime;
}