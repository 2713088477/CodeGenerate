package com.hao.backend.core;


import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.hao.backend.model.enums.CodeGenTypeEnum;

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


}