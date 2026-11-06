package com.istream.generator.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * 代码生成器模块自动配置
 *
 * <p>生产环境默认关闭，避免暴露数据库元数据查询端点。</p>
 *
 * @author istream
 * @since 2026-10-26
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "istream.generator", name = "enabled", havingValue = "true")
public class GeneratorAutoConfiguration {

    public GeneratorAutoConfiguration() {
        log.info("GeneratorAutoConfiguration: 代码生成器模块已启用");
    }
}