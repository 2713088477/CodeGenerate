package com.hao.backend.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hao.backend.common.BusinessException;
import com.hao.backend.common.ResponseCode;
import com.hao.backend.core.CodeFileSaver;
import com.hao.backend.model.enums.CodeGenTypeEnum;
import com.hao.backend.model.vo.AppVO;
import com.hao.backend.service.AppService;
import com.hao.backend.service.AuthService;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;

/**
 * 应用预览：把已生成的代码目录按相对路径静态返回，供前端 iframe 加载。
 * 只有创建者本人能访问。多文件模式下 index.html 通过相对路径引用 style.css 和 script.js，
 * 所以访问入口必须带结尾斜杠：/app/preview/{id}/
 * <p>
 * 这里不走统一的 JSON 包装（那个固定返回 HTTP 200），而是直接返回真实的 HTTP 状态码，
 * 这样 iframe 和前端的“是否已生成”探测都能靠状态码判断。
 */
@Hidden
@RestController
@RequestMapping("/app/preview")
@RequiredArgsConstructor
public class AppPreviewController {

    private static final String DEFAULT_FILE = "index.html";

    private static final Map<String, String> CONTENT_TYPES = Map.ofEntries(
            Map.entry("html", "text/html;charset=UTF-8"),
            Map.entry("htm", "text/html;charset=UTF-8"),
            Map.entry("css", "text/css;charset=UTF-8"),
            Map.entry("js", "text/javascript;charset=UTF-8"),
            Map.entry("json", "application/json;charset=UTF-8"),
            Map.entry("txt", "text/plain;charset=UTF-8"),
            Map.entry("svg", "image/svg+xml"),
            Map.entry("png", "image/png"),
            Map.entry("jpg", "image/jpeg"),
            Map.entry("jpeg", "image/jpeg"),
            Map.entry("gif", "image/gif"),
            Map.entry("webp", "image/webp"),
            Map.entry("ico", "image/x-icon"),
            Map.entry("woff", "font/woff"),
            Map.entry("woff2", "font/woff2"));

    private final AppService appService;
    private final AuthService authService;

    @GetMapping("/{id}/{*path}")
    public ResponseEntity<byte[]> preview(
            @Parameter(schema = @Schema(type = "string")) @PathVariable Long id,
            @PathVariable String path) {
        // 没带结尾斜杠时补上，否则 style.css 这类相对路径会解析错
        if (path.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, id + "/").build();
        }

        AppVO app;
        try {
            app = appService.getMyApp(id, authService.getCurrentUser().getId());
        } catch (BusinessException e) {
            return fail(statusOf(e), e.getMessage());
        }
        CodeGenTypeEnum codeGenType = CodeGenTypeEnum.fromValue(app.getCodeGenType());
        if (codeGenType == null) {
            return fail(HttpStatus.INTERNAL_SERVER_ERROR, "代码生成类型不正确");
        }

        Path codeDir = CodeFileSaver.getCodeDir(codeGenType, id).toAbsolutePath().normalize();
        String relative = path.startsWith("/") ? path.substring(1) : path;
        if (relative.isEmpty()) {
            relative = DEFAULT_FILE;
        }

        Path file = codeDir.resolve(relative).normalize();
        // normalize 之后必须仍在代码目录内，防止 ../ 目录穿越
        if (!file.startsWith(codeDir) || !Files.isRegularFile(file)) {
            return fail(HttpStatus.NOT_FOUND, "文件不存在");
        }

        byte[] body;
        try {
            body = Files.readAllBytes(file);
        } catch (IOException e) {
            return fail(HttpStatus.INTERNAL_SERVER_ERROR, "读取文件失败");
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentTypeOf(file)))
                .cacheControl(CacheControl.noStore())
                .body(body);
    }

    private static HttpStatus statusOf(BusinessException e) {
        Integer code = e.getCode();
        if (ResponseCode.NOT_FOUND.getCode().equals(code)) {
            return HttpStatus.NOT_FOUND;
        }
        if (ResponseCode.FORBIDDEN.getCode().equals(code)) {
            return HttpStatus.FORBIDDEN;
        }
        if (ResponseCode.UNAUTHORIZED.getCode().equals(code)) {
            return HttpStatus.UNAUTHORIZED;
        }
        return HttpStatus.BAD_REQUEST;
    }

    private static ResponseEntity<byte[]> fail(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .contentType(MediaType.parseMediaType("text/plain;charset=UTF-8"))
                .cacheControl(CacheControl.noStore())
                .body(message.getBytes(StandardCharsets.UTF_8));
    }

    private static String contentTypeOf(Path file) {
        String name = file.getFileName().toString();
        int dot = name.lastIndexOf('.');
        String ext = dot < 0 ? "" : name.substring(dot + 1).toLowerCase();
        return CONTENT_TYPES.getOrDefault(ext, MediaType.APPLICATION_OCTET_STREAM_VALUE);
    }
}
