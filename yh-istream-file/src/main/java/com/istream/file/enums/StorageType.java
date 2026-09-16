package com.istream.file.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StorageType {

    LOCAL("LOCAL", "本地存储"),
    MINIO("MINIO", "MinIO 对象存储"),
    ALIYUN_OSS("ALIYUN_OSS", "阿里云 OSS"),
    TENCENT_COS("TENCENT_COS", "腾讯云 COS");

    @EnumValue
    private final String value;

    private final String description;
}