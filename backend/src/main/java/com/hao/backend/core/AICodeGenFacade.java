package com.hao.backend.core;

import java.nio.file.Path;

import org.springframework.stereotype.Service;

import com.hao.backend.ai.AICodeGenService;
import com.hao.backend.ai.model.HtmlCodeResult;
import com.hao.backend.ai.model.MultiFileCodeResult;
import com.hao.backend.common.BusinessException;
import com.hao.backend.common.ResponseCode;
import com.hao.backend.common.ThrowUtil;
import com.hao.backend.model.enums.CodeGenTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * AI生成代码的门面类，根据不同的模式生成代码，并保存到本地文件。
 */
@Service
@RequiredArgsConstructor  
@Slf4j 
public class AICodeGenFacade {
    private final AICodeGenService aiCodeGenService;
    
    /**
     * 统一入口：根据类型生成并保存代码
     * @param userMessage 用户提示词
     * @param codeGenType 生成类型
     * @param appId 应用 id，用于代码保存目录名
     * @return
     */
    public Path generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenType, Long appId) {
        ThrowUtil.throwIf(codeGenType == null, new BusinessException(ResponseCode.SYSTEM_ERROR, "生成类型为空"));
        ThrowUtil.throwIf(appId == null, new BusinessException(ResponseCode.PARAMS_ERROR, "应用id不能为空"));
        return switch (codeGenType) {
            case HTML -> generateAndSaveHtmlCode(userMessage, appId);
            case MULTI_FILE -> generateAndSaveMultiFileCode(userMessage, appId);
            default -> throw new BusinessException(ResponseCode.SYSTEM_ERROR, "生成类型不支持");
        };

    }

    /**
     * 统一入口：根据类型生成并保存代码(流式)
     * @param userMessage 用户提示词
     * @param codeGenType 生成类型
     * @param appId 应用 id，用于代码保存目录名
     * @return
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenType, Long appId) {
        ThrowUtil.throwIf(codeGenType == null, new BusinessException(ResponseCode.SYSTEM_ERROR, "生成类型为空"));
        ThrowUtil.throwIf(appId == null, new BusinessException(ResponseCode.PARAMS_ERROR, "应用id不能为空"));
        return switch (codeGenType) {
            case HTML -> generateAndSaveHtmlCodeStream(userMessage, appId);
            case MULTI_FILE -> generateAndSaveMultiFileCodeStream(userMessage, appId);
            default -> throw new BusinessException(ResponseCode.SYSTEM_ERROR, "生成类型不支持");
        };

    }


    private Path generateAndSaveHtmlCode(String userMessage, Long appId) {
        HtmlCodeResult result = aiCodeGenService.generateHtmlCode(userMessage);
        return CodeFileSaver.saveHtmlCodeResult(result, appId);
    }

    private Path generateAndSaveMultiFileCode(String userMessage, Long appId) {
        MultiFileCodeResult result = aiCodeGenService.generateMultiFileCode(userMessage);
        return CodeFileSaver.saveMultiFileCodeResult(result, appId);
    }

    private Flux<String> generateAndSaveHtmlCodeStream(String userMessage, Long appId) {
        return Flux.defer(()->{
            StringBuilder builder = new StringBuilder();
            return aiCodeGenService.generateHtmlCodeStream(userMessage)
                .doOnNext(builder::append)
                .doOnComplete(()->{
                    HtmlCodeResult result = CodeParser.parseHtmlCode(builder.toString());
                    CodeFileSaver.saveHtmlCodeResult(result, appId);
                })
                .doOnError(e -> log.error("流式生成html代码失败",e))
                .onErrorMap(e -> new BusinessException(ResponseCode.SYSTEM_ERROR, "流式生成html代码失败"));
        });
    }

    private Flux<String> generateAndSaveMultiFileCodeStream(String userMessage, Long appId) {
        return Flux.defer(()->{
            StringBuilder builder = new StringBuilder();
            return aiCodeGenService.generateMultiFileCodeStream(userMessage)
                .doOnNext(builder::append)
                .doOnComplete(()->{
                    MultiFileCodeResult result = CodeParser.parseMultiFileCode(builder.toString());
                    CodeFileSaver.saveMultiFileCodeResult(result, appId);
                })
                .doOnError(e -> log.error("流式生成多文件代码失败",e))
                .onErrorMap(e -> new BusinessException(ResponseCode.SYSTEM_ERROR, "流式生成多文件代码失败"));
        });
    }
}
