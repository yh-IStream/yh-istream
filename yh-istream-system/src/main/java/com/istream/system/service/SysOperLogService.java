package com.istream.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.spring.service.IService;
import com.istream.system.model.query.SysOperLogQuery;
import com.istream.system.entity.SysOperLog;

import java.time.LocalDateTime;

/**
 * 操作日志服务接口
 *
 * @author istream
 * @since 2026-08-17
 */
public interface SysOperLogService extends IService<SysOperLog> {

    /**
     * 分页查询操作日志
     *
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<SysOperLog> page(SysOperLogQuery query);

    /**
     * 清空操作日志
     */
    void truncate();

    /**
     * 统计今日操作次数
     *
     * @param todayStart 今日起始时间
     * @return 操作次数
     */
    long countTodayOps(LocalDateTime todayStart);

    /**
     * 查询全部操作日志（导出用，按操作时间倒序）
     *
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    IPage<SysOperLog> pageExport(long pageNum, long pageSize);
}