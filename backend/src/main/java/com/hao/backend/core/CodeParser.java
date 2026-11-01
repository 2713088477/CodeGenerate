package com.hao.backend.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hao.backend.ai.model.HtmlCodeResult;
import com.hao.backend.ai.model.MultiFileCodeResult;
import com.hao.backend.common.BusinessException;
import com.hao.backend.common.ResponseCode;

import lombok.extern.slf4j.Slf4j;

/**
 * 把流式输出拼接得到的完整 JSON 文本解析成结构化结果。
 * 输入: JSON 文本，字段与 Result 对象一致（htmlCode / cssCode / jsCode / description）。
 * 输出: {@link HtmlCodeResult} 或 {@link MultiFileCodeResult}
 */
@Slf4j
public class CodeParser {

    private static final String FENCE = "```";

    /**
     * 忽略 AI 可能多返回的未知字段
     */
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private CodeParser() {
    }

    /**
     * 解析单 HTML 模式的 JSON 输出。
     * @throws BusinessException 内容为空、不是合法 JSON，或 htmlCode 为空时抛出
     */
    public static HtmlCodeResult parseHtmlCode(String text) {
        HtmlCodeResult result = readJson(text, HtmlCodeResult.class);
        requireHtml(result.getHtmlCode());
        return result;
    }

    /**
     * 解析多文件模式的 JSON 输出。htmlCode 必须有内容；cssCode、jsCode 为空时给出警告，由 {@link CodeFileSaver} 写成空文件。
     * @throws BusinessException 内容为空、不是合法 JSON，或 htmlCode 为空时抛出
     */
    public static MultiFileCodeResult parseMultiFileCode(String text) {
        MultiFileCodeResult result = readJson(text, MultiFileCodeResult.class);
        requireHtml(result.getHtmlCode());
        if (result.getCssCode() == null || result.getCssCode().isBlank()) {
            log.warn("AI返回的JSON中cssCode为空");
        }
        if (result.getJsCode() == null || result.getJsCode().isBlank()) {
            log.warn("AI返回的JSON中jsCode为空");
        }
        return result;
    }

    private static <T> T readJson(String text, Class<T> type) {
        if (text == null || text.isBlank()) {
            throw new BusinessException(ResponseCode.SYSTEM_ERROR, "AI返回内容为空");
        }
        try {
            return JSON_MAPPER.readValue(stripJsonFence(text), type);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ResponseCode.SYSTEM_ERROR, "AI返回内容不是合法的JSON：" + e.getOriginalMessage());
        }
    }

    private static void requireHtml(String html) {
        if (html == null || html.isBlank()) {
            throw new BusinessException(ResponseCode.SYSTEM_ERROR, "未能从AI返回内容中解析出HTML代码");
        }
    }

    /**
     * 兜底：模型偶尔会把 JSON 包在 ```json 围栏里，这里去掉最外层围栏
     */
    private static String stripJsonFence(String text) {
        String s = text.strip();
        if (s.startsWith(FENCE)) {
            int firstLineEnd = s.indexOf('\n');
            s = firstLineEnd < 0 ? "" : s.substring(firstLineEnd + 1);
            if (s.stripTrailing().endsWith(FENCE)) {
                s = s.stripTrailing();
                s = s.substring(0, s.length() - FENCE.length());
            }
        }
        return s.strip();
    }
}
