package com.hao.backend.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.hao.backend.ai.model.HtmlCodeResult;
import com.hao.backend.ai.model.MultiFileCodeResult;
import com.hao.backend.common.BusinessException;
import com.hao.backend.common.ResponseCode;
import com.hao.backend.model.enums.CodeGenTypeEnum;

/**
 * 把 AI 生成的代码结果保存到本地文件。
 * 目录结构：{FILE_SAVE_ROOT_DIR}/{type.value}_{appId}/文件名
 */
public class CodeFileSaver {

    private static final String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";

    private CodeFileSaver() {
    }

    /**
     * 保存单 HTML 结果，生成 index.html
     * @param appId 应用 id，作为目录名的一部分
     * @return 本次保存所在的目录
     */
    public static Path saveHtmlCodeResult(HtmlCodeResult result, Long appId) {
        if (result == null) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR, "代码结果不能为空");
        }
        Path dir = buildDir(CodeGenTypeEnum.HTML, appId);
        writeFile(dir, "index.html", result.getHtmlCode());
        return dir;
    }

    /**
     * 保存多文件结果，生成 index.html、style.css、script.js
     * @param appId 应用 id，作为目录名的一部分
     * @return 本次保存所在的目录
     */
    public static Path saveMultiFileCodeResult(MultiFileCodeResult result, Long appId) {
        if (result == null) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR, "代码结果不能为空");
        }
        Path dir = buildDir(CodeGenTypeEnum.MULTI_FILE, appId);
        writeFile(dir, "index.html", result.getHtmlCode());
        writeFile(dir, "style.css", result.getCssCode());
        writeFile(dir, "script.js", result.getJsCode());
        return dir;
    }

    /**
     * 创建父目录：{type.value}_{appId}。同一应用再次生成时目录已存在，直接复用并覆盖里面的文件。
     */
    private static Path buildDir(CodeGenTypeEnum type, Long appId) {
        if (appId == null) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR, "应用id不能为空");
        }
        Path dir = Path.of(FILE_SAVE_ROOT_DIR, type.getValue() + "_" + appId);
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new BusinessException(ResponseCode.INTERNAL_SERVER_ERROR, "创建目录失败：" + e.getMessage());
        }
        return dir;
    }

    /**
     * 以 UTF-8 写入文件，内容为 null 时写空文件（保证 index.html 引用的 css/js 不会 404）
     */
    private static void writeFile(Path dir, String fileName, String content) {
        try {
            Files.writeString(dir.resolve(fileName), content == null ? "" : content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new BusinessException(ResponseCode.INTERNAL_SERVER_ERROR,
                    "保存文件失败：" + fileName + "，" + e.getMessage());
        }
    }
}
