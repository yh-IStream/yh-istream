package com.istream.framework.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.ToStringSerializer;

/**
 * Jackson 全局配置
 * <p>
 * 将 Long 包装类型序列化为 JSON 字符串，防止 JavaScript 数字精度丢失。
 * <p>
 * 仅注册 Long.class（包装类型），不注册 Long.TYPE（原始类型）。
 * 原因：实体 ID 字段均为 Long 包装类型，需要转为 String 防精度丢失；
 * 而 MyBatis-Plus Page 的 total/size/current/pages 为 long 原始类型，
 * 其值远小于 2^53，序列化为 Number 更便于前端直接使用。
 * <p>
 * 通过 {@link JsonMapperBuilderCustomizer} 在 Spring Boot 构建
 * {@link tools.jackson.databind.json.JsonMapper} 时注册自定义 Module，
 * 确保序列化器对实体字段、{@code List<Long>} 元素、{@code Map<Long, ?>} 键值等场景均生效。
 *
 * @author istream
 * @since 2026-08-28
 */
@Configuration
public class JacksonConfig {

    @Bean
    public JsonMapperBuilderCustomizer longToStringCustomizer() {
        SimpleModule module = new SimpleModule("longToStringModule");
        module.addSerializer(Long.class, ToStringSerializer.instance);
        return builder -> builder.addModule(module);
    }
}