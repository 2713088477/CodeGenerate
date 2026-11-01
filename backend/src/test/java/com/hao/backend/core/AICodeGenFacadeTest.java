package com.hao.backend.core;


import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.hao.backend.model.enums.CodeGenTypeEnum;

import reactor.core.publisher.Flux;

@SpringBootTest
class AICodeGenFacadeTest {

    @Autowired
    private AICodeGenFacade aiCodeGenFacade;

    @Test
    void testHtmlCodeGen() {
        String userMessage = "生成一个简单的HTML页面，不超过300行";
        CodeGenTypeEnum codeGenType = CodeGenTypeEnum.HTML;
        Path path = aiCodeGenFacade.generateAndSaveCode(userMessage, codeGenType);
        System.out.println(path);
    }

    @Test 
    void testMultiFileCodeGen() {
        String userMessage = "生成一个简单的博客网站，三个文件不超过500行";
        CodeGenTypeEnum codeGenType = CodeGenTypeEnum.MULTI_FILE;
        Path path = aiCodeGenFacade.generateAndSaveCode(userMessage, codeGenType);
        System.out.println(path);
    }

    @Test
    void testHtmlCodeGenStream() {
        String userMessage = "生成一个记录食物卡路里的网站，不超过700行";
        CodeGenTypeEnum codeGenType = CodeGenTypeEnum.HTML;
        Flux<String> flux = aiCodeGenFacade.generateAndSaveCodeStream(userMessage, codeGenType);
        //阻塞直到flux完成
        flux.blockLast();
    }

    @Test 
    void testMultiFileCodeGenStream() {
        String userMessage = "给我生成一个让人眼前一亮，炫酷的网站，不超过1000行";
        CodeGenTypeEnum codeGenType = CodeGenTypeEnum.MULTI_FILE;
        Flux<String> flux = aiCodeGenFacade.generateAndSaveCodeStream(userMessage, codeGenType);
        
        // 收集所有的chunk,拼接成完整输出
        List<String> chunks = flux.collectList().block(Duration.ofMinutes(5));
        String result = String.join("", chunks);
        System.out.println(result);
    }


}