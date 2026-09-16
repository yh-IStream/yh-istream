package com.istream.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.spring.service.IService;
import com.istream.system.model.query.SysDictDataQuery;
import com.istream.system.entity.SysDictData;

import java.util.List;
import java.util.Map;

/**
 * 字典数据服务接口
 *
 * @author istream
 * @since 2026-08-17
 */
public interface SysDictDataService extends IService<SysDictData> {

    /**
     * 分页查询字典数据
     *
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<SysDictData> page(SysDictDataQuery query);

    /**
     * 获取所有字典数据（按类型分组）
     *
     * @return 字典类型 → 字典数据列表
     */
    Map<String, List<SysDictData>> getDictMap();

    /**
     * 根据字典类型查询启用的字典数据
     *
     * @param dictType 字典类型
     * @return 字典数据列表
     */
    List<SysDictData> listByType(String dictType);
}