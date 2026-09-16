package com.istream.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.spring.service.IService;
import com.istream.system.model.query.SysLoginInfoQuery;
import com.istream.system.entity.SysLoginInfo;

/**
 * 登录日志服务接口
 *
 * @author istream
 * @since 2026-08-17
 */
public interface SysLoginInfoService extends IService<SysLoginInfo> {

    /**
     * 分页查询登录日志
     *
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<SysLoginInfo> page(SysLoginInfoQuery query);

    /**
     * 清空登录日志
     */
    void truncate();

    /**
     * 查询全部登录日志（导出用，按登录时间倒序）
     *
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    IPage<SysLoginInfo> pageExport(long pageNum, long pageSize);
}