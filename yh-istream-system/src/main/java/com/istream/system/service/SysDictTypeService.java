package com.istream.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.spring.service.IService;
import com.istream.system.model.query.dict.SysDictTypeQuery;
import com.istream.system.entity.SysDictType;

/**
 * 字典类型服务接口
 *
 * @author istream
 * @since 2026-08-17
 */
public interface SysDictTypeService extends IService<SysDictType> {

    /**
     * 分页查询字典类型
     *
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<SysDictType> page(SysDictTypeQuery query);

    /**
     * 判断字典类型是否已存在
     *
     * @param dictType 字典类型
     * @param excludeId 排除的字典类型ID（修改时排除自身，可为null）
     * @return 是否存在
     */
    boolean existsByDictType(String dictType, Long excludeId);
}