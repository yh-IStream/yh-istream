package com.istream.framework.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson 全局配置
 * <p>
 * 将所有 Long 类型序列化为 JSON 字符串，防止 JavaScript 数字精度丢失。
 * 本项目使用 MyBatis-Plus ASSIGN_ID 策略生成 Snowflake 雪花 ID（19 位），
 * JavaScript 的 number 类型（IEEE 754 双精度）只能精确表示 2^53-1 以内的整数，
 * 超出范围的 ID 会被截断，导致前后端数据不一致。
 *
 * @author isteam
 * @since 2026-08-28
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Module longToStringModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Long.class, ToStringSerializer.instance);
        module.addSerializer(Long.TYPE, ToStringSerializer.instance);
        return module;
    }
}