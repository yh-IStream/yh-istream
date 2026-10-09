package com.istream.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.IService;
import com.istream.system.model.dto.config.SysConfigSaveDTO;
import com.istream.system.model.query.config.SysConfigQuery;
import com.istream.system.entity.SysConfig;

/**
 * 系统配置服务接口
 *
 * @author istream
 * @since 2026-08-17
 */
public interface SysConfigService extends IService<SysConfig> {

    /**
     * 分页查询配置
     *
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<SysConfig> page(SysConfigQuery query);

    /**
     * 判断配置键是否已存在
     *
     * @param configKey 配置键
     * @param excludeId 排除的配置ID（修改时排除自身，可为null）
     * @return 是否存在
     */
    boolean existsByConfigKey(String configKey, Long excludeId);

    /**
     * 根据配置键查询配置值
     *
     * @param configKey 配置键
     * @return 配置值
     */
    String getConfigValueByKey(String configKey);

    /**
     * 新增配置（含唯一性校验）
     *
     * @param dto 配置保存 DTO
     */
    void createConfig(SysConfigSaveDTO dto);

    /**
     * 修改配置（含唯一性校验）
     *
     * @param dto 配置保存 DTO
     */
    void updateConfig(SysConfigSaveDTO dto);

    /**
     * 清除配置缓存
     */
    void clearConfigCache();
}