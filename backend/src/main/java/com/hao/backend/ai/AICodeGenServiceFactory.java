package com.hao.backend.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AICodeGenServiceFactory {

    @Resource
    private ChatModel chatModel;

    @Bean
    public AICodeGenService codeGenService(){
        return AiServices.create(AICodeGenService.class,chatModel);
    }
}
