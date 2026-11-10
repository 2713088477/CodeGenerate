package com.hao.backend.controller;

import com.hao.backend.common.BaseResponse;
import com.hao.backend.common.PageResult;
import com.hao.backend.model.dto.app.AdminAppQueryRequest;
import com.hao.backend.model.dto.app.AdminAppUpdateRequest;
import com.hao.backend.model.dto.app.AppChatRequest;
import com.hao.backend.model.dto.app.AppCreateRequest;
import com.hao.backend.model.dto.app.AppQueryRequest;
import com.hao.backend.model.dto.app.AppUpdateRequest;
import com.hao.backend.model.vo.AdminAppVO;
import com.hao.backend.model.vo.AppVO;
import com.hao.backend.service.AppService;
import com.hao.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 应用表 控制层。
 *
 * @author <a href="https://github.com/2713088477">艺艺生辉</a>
 */
@RestController
@RequestMapping("/app")
@RequiredArgsConstructor
public class AppController {

    private final AppService appService;
    private final AuthService authService;

    @PostMapping("/add")
    public BaseResponse<Long> addApp(@RequestBody @Valid AppCreateRequest request) {
        Long appId = appService.createApp(request, authService.getCurrentUser().getId());
        return BaseResponse.success(appId);
    }

    @GetMapping("/my/page")
    public BaseResponse<PageResult<AppVO>> pageMyApps(@Valid AppQueryRequest query) {
        return BaseResponse.success(appService.pageMyApps(query, authService.getCurrentUser().getId()));
    }

    @GetMapping("/admin/page")
    @PreAuthorize("hasRole('ADMIN')")
    public BaseResponse<PageResult<AdminAppVO>> pageAllApps(@Valid AdminAppQueryRequest query) {
        return BaseResponse.success(appService.pageAllApps(query));
    }

    @PutMapping("/{id}")
    public BaseResponse<Void> updateMyApp(
            @Parameter(schema = @Schema(type = "string")) @PathVariable Long id,
            @RequestBody @Valid AppUpdateRequest request) {
        appService.updateMyApp(id, request, authService.getCurrentUser().getId());
        return BaseResponse.success();
    }

    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public BaseResponse<Void> adminUpdateApp(
            @Parameter(schema = @Schema(type = "string")) @PathVariable Long id,
            @RequestBody @Valid AdminAppUpdateRequest request) {
        appService.adminUpdateApp(id, request);
        return BaseResponse.success();
    }

    @DeleteMapping("/{id}")
    public BaseResponse<Void> deleteMyApp(@Parameter(schema = @Schema(type = "string")) @PathVariable Long id) {
        appService.deleteMyApp(id, authService.getCurrentUser().getId());
        return BaseResponse.success();
    }

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public BaseResponse<Void> adminDeleteApp(@Parameter(schema = @Schema(type = "string")) @PathVariable Long id) {
        appService.adminDeleteApp(id);
        return BaseResponse.success();
    }

    /**
     * 创建者对话生成代码，以 SSE 流式返回模型输出。
     */
    @PostMapping(value = "/chat/gen", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatToGenCode(@RequestBody @Valid AppChatRequest request) {
        return appService.chatToGenCode(request, authService.getCurrentUser().getId());
    }
}
