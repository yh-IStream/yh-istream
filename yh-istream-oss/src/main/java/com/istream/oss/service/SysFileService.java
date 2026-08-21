package com.istream.oss.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.istream.oss.entity.SysFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface SysFileService extends IService<SysFile> {

    SysFile upload(MultipartFile file, String module);

    InputStream getFileStream(Long id);

    void deleteFile(Long id);
}