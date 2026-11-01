package com.hao.backend.ai;

import com.hao.backend.ai.model.HtmlCodeResult;
import com.hao.backend.ai.model.MultiFileCodeResult;

import dev.langchain4j.service.SystemMessage;
import reactor.core.publisher.Flux;

/**
 * 同步和流式调用共用同一份系统提示词，提示词里已经写明了JSON输出格式。
 * 注意：LangChain4j 的 AI Service 不支持流式返回 POJO，参考 <a href="https://docs.langchain4j.dev/tutorials/structured-outputs">Structured Outputs</a>。
 * 所以流式方法返回的是 JSON 文本片段，流结束后由 Facade 拼接并解析成对应的 Result 对象。
 * 流式生效依赖 streaming-chat-model 的 response-format: json_object 配置。
 */
public interface AICodeGenService {

    /**
     * 生成单html代码
     * @param userMessage 用户消息
     * @return 生成的代码结果
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    HtmlCodeResult generateHtmlCode(String userMessage);

    /**
     * 生成多文件代码
     * @param userMessage 用户消息
     * @return 生成的代码结果
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    MultiFileCodeResult generateMultiFileCode(String userMessage);

    /**
     * 生成单html代码(流式)。返回的是 JSON 文本片段，拼接完整后可解析为 {@link HtmlCodeResult}
     * @param userMessage 用户消息
     * @return 流式返回的结果
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    Flux<String> generateHtmlCodeStream(String userMessage);

    /**
     * 生成多文件代码(流式)。返回的是 JSON 文本片段，拼接完整后可解析为 {@link MultiFileCodeResult}
     * @param userMessage 用户消息
     * @return 流式返回的结果
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    Flux<String> generateMultiFileCodeStream(String userMessage);
}
