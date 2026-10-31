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
import com.mybatisflex.core.keygen.impl.SnowFlakeIDKeyGenerator;

/**
 * 把 AI 生成的代码结果保存到本地文件。
 * 目录结构：{FILE_SAVE_ROOT_DIR}/{type.value}_{雪花id}/文件名
 */
public class CodeFileSaver {

    private static final String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";

    /**
     * 复用 MyBatis-Flex 自带的雪花 ID 生成器（nextId 是 synchronized 的，线程安全）
     */
    private static final SnowFlakeIDKeyGenerator ID_GENERATOR = new SnowFlakeIDKeyGenerator();

    private CodeFileSaver() {
    }

    /**
     * 保存单 HTML 结果，生成 index.html
     * @return 本次保存所在的目录
     */
    public static Path saveHtmlCodeResult(HtmlCodeResult result) {
        if (result == null) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR, "代码结果不能为空");
        }
        Path dir = buildUniqueDir(CodeGenTypeEnum.HTML);
        writeFile(dir, "index.html", result.getHtmlCode());
        return dir;
    }

    /**
     * 保存多文件结果，生成 index.html、style.css、script.js
     * @return 本次保存所在的目录
     */
    public static Path saveMultiFileCodeResult(MultiFileCodeResult result) {
        if (result == null) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR, "代码结果不能为空");
        }
        Path dir = buildUniqueDir(CodeGenTypeEnum.MULTI_FILE);
        writeFile(dir, "index.html", result.getHtmlCode());
        writeFile(dir, "style.css", result.getCssCode());
        writeFile(dir, "script.js", result.getJsCode());
        return dir;
    }

    /**
     * 创建父目录：{type.value}_{雪花id}
     */
    private static Path buildUniqueDir(CodeGenTypeEnum type) {
        Path dir = Path.of(FILE_SAVE_ROOT_DIR, type.getValue() + "_" + ID_GENERATOR.nextId());
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
