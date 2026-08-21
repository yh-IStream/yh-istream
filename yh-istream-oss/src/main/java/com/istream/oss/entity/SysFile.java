package com.istream.oss.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.istream.common.model.BaseEntity;
import com.istream.oss.enums.StorageType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_file")
public class SysFile extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String fileName;

    private String originalName;

    private String filePath;

    private Long fileSize;

    private String mimeType;

    private String fileExt;

    private StorageType storageType;

    private String storageUrl;

    private String module;
}