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

/**
 * AI生成代码的门面类，根据不同的模式生成代码，并保存到本地文件。
 */
@Service
@RequiredArgsConstructor  
public class AICodeGenFacade {
    private final AICodeGenService aiCodeGenService;
    
    /**
     * 统一入口：根据类型生成并保存代码
     * @param userMessage 用户提示词
     * @param codeGenType 生成类型
     * @return
     */
    public Path generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenType) {
        ThrowUtil.throwIf(codeGenType == null, new BusinessException(ResponseCode.SYSTEM_ERROR, "生成类型为空"));
        return switch (codeGenType) {
            case HTML -> generateAndSavaHtmlCode(userMessage);
            case MULTI_FILE -> generateAndSavaMultiFileCode(userMessage);
            default -> throw new BusinessException(ResponseCode.SYSTEM_ERROR, "生成类型不支持");
        };

    }


    private Path generateAndSavaHtmlCode(String userMessage) {
        HtmlCodeResult result = aiCodeGenService.generateHtmlCode(userMessage);
        return CodeFileSaver.saveHtmlCodeResult(result);
    }

    private Path generateAndSavaMultiFileCode(String userMessage) {
        MultiFileCodeResult result = aiCodeGenService.generateMultiFileCode(userMessage);
        return CodeFileSaver.saveMultiFileCodeResult(result);
    }
}
