package com.istream.system.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.istream.system.entity.SysConfig;

public interface SysConfigService extends IService<SysConfig> {

    String getConfigValueByKey(String configKey);

    void clearConfigCache();
}