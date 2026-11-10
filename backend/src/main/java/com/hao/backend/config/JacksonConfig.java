package com.hao.backend.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.ToStringSerializer;

/**
 * 全局把 Long 序列化成字符串，避免前端处理时精度丢失。
 * 所以统一输出字符串，前端不再需要逐个字段处理。
 * 只影响响应的序列化，请求里的数字和字符串都能正常反序列化成 Long。
 */
@Configuration
public class JacksonConfig {

    @Bean
    public JsonMapperBuilderCustomizer longToStringCustomizer() {
        return builder -> {
            SimpleModule module = new SimpleModule();
            module.addSerializer(Long.class, ToStringSerializer.instance);
            module.addSerializer(long.class, ToStringSerializer.instance);
            builder.addModule(module);
        };
    }
}
