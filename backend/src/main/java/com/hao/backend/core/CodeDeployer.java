package com.hao.backend.core;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

import com.hao.backend.common.BusinessException;
import com.hao.backend.common.ResponseCode;
import com.hao.backend.constant.AppConstant;

/**
 * 把代码生成目录复制到部署目录：{CODE_DEPLOY_ROOT_DIR}/{deployKey}，之后由 Nginx 静态托管。
 */
public class CodeDeployer {

    private CodeDeployer() {
    }

    /**
     * 获取某个部署标识对应的部署目录，只拼路径，不创建目录。
     */
    public static Path getDeployDir(String deployKey) {
        return Path.of(AppConstant.CODE_DEPLOY_ROOT_DIR, deployKey);
    }

    /**
     * 复制代码目录到部署目录。部署目录已存在时直接覆盖里面的同名文件（重新部署拿到最新代码）。
     * @param sourceDir 代码生成目录，必须存在
     * @param deployKey 部署标识，作为部署目录名
     * @return 部署目录
     */
    public static Path deploy(Path sourceDir, String deployKey) {
        Path targetDir = getDeployDir(deployKey);
        try {
            Files.createDirectories(targetDir);
            Files.walkFileTree(sourceDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Files.createDirectories(targetDir.resolve(sourceDir.relativize(dir).toString()));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.copy(file, targetDir.resolve(sourceDir.relativize(file).toString()),
                            StandardCopyOption.REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new BusinessException(ResponseCode.INTERNAL_SERVER_ERROR, "部署失败：" + e.getMessage());
        }
        return targetDir;
    }
}
