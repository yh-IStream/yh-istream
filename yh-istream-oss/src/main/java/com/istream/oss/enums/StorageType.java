package com.istream.oss.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StorageType {

    LOCAL("本地存储"),
    MINIO("MinIO 对象存储"),
    ALIYUN_OSS("阿里云 OSS"),
    TENCENT_COS("腾讯云 COS");

    private final String description;
}