package com.istream.file.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.istream.common.model.BaseEntity;
import com.istream.file.enums.StorageType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_file")
public class SysFile extends BaseEntity {

    /** 存储文件名 */
    private String fileName;

    /** 原始文件名 */
    private String originalName;

    /** 文件相对路径 */
    private String filePath;

    /** 文件大小（字节） */
    private Long fileSize;

    /** MIME 类型 */
    private String mimeType;

    /** 文件扩展名 */
    private String fileExt;

    /** 存储类型 */
    private StorageType storageType;

    /** 访问 URL */
    private String storageUrl;

    /** 所属模块 */
    private String module;
}