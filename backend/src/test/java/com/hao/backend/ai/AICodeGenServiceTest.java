package com.hao.backend.ai;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.hao.backend.ai.model.MultiFileCodeResult;


@SpringBootTest
class AICodeGenServiceTest {

    @Resource
    AICodeGenService codeGenService;

    @Test
    void generateHtmlCode() {
        System.out.println(codeGenService.generateHtmlCode("生成一个简单的登录页面，不超过50行代码"));
    }

    @Test
    void generateMultiFileCode() {
        MultiFileCodeResult result = codeGenService.generateMultiFileCode("生成一个的博客网站，三个文件不超过200行代码");
        System.out.println(result);
    }


}