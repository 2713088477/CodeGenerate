package com.hao.backend.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.hao.backend.ai.model.HtmlCodeResult;
import com.hao.backend.ai.model.MultiFileCodeResult;
import com.hao.backend.common.BusinessException;

/**
 * 纯单元测试，不启动 Spring，不调用 AI。
 */
class CodeParserTest {

    @Test
    void parseMultiFileCode_normal() {
        String json = "{\"htmlCode\":\"<html></html>\",\"cssCode\":\"a{}\",\"jsCode\":\"let s = `x`;\",\"description\":\"d\"}";
        MultiFileCodeResult result = CodeParser.parseMultiFileCode(json);
        assertEquals("<html></html>", result.getHtmlCode());
        assertEquals("a{}", result.getCssCode());
        assertEquals("let s = `x`;", result.getJsCode());
        assertEquals("d", result.getDescription());
    }

    @Test
    void parseMultiFileCode_wrappedInJsonFence_andUnknownFields() {
        String json = "```json\n{\"htmlCode\":\"<html></html>\",\"extra\":1}\n```";
        MultiFileCodeResult result = CodeParser.parseMultiFileCode(json);
        assertEquals("<html></html>", result.getHtmlCode());
        assertNull(result.getCssCode());
    }

    @Test
    void parseHtmlCode_normal() {
        HtmlCodeResult result = CodeParser.parseHtmlCode("{\"htmlCode\":\"<html></html>\",\"description\":\"d\"}");
        assertEquals("<html></html>", result.getHtmlCode());
        assertEquals("d", result.getDescription());
    }

    @Test
    void parse_blankInvalidOrNoHtml_throws() {
        assertThrows(BusinessException.class, () -> CodeParser.parseMultiFileCode(null));
        assertThrows(BusinessException.class, () -> CodeParser.parseMultiFileCode("   \n   "));
        assertThrows(BusinessException.class, () -> CodeParser.parseMultiFileCode("这不是json"));
        assertThrows(BusinessException.class, () -> CodeParser.parseMultiFileCode("{\"cssCode\":\"a{}\"}"));
        assertThrows(BusinessException.class, () -> CodeParser.parseHtmlCode("{\"htmlCode\":\"  \"}"));
    }
}
