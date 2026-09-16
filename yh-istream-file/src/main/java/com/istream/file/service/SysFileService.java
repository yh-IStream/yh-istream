package com.istream.file.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.spring.service.IService;
import com.istream.file.model.query.SysFileQuery;
import com.istream.file.entity.SysFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * 文件服务接口
 *
 * @author istream
 * @since 2026-08-17
 */
public interface SysFileService extends IService<SysFile> {

    /**
     * 分页查询文件列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<SysFile> page(SysFileQuery query);

    /**
     * 上传文件
     *
     * @param file   文件
     * @param module 模块名
     * @return 文件实体
     */
    SysFile upload(MultipartFile file, String module);

    /**
     * 获取文件流
     *
     * @param id 文件ID
     * @return 文件输入流
     */
    InputStream getFileStream(Long id);

    /**
     * 删除文件（含物理文件）
     *
     * @param id 文件ID
     */
    void deleteFile(Long id);
}