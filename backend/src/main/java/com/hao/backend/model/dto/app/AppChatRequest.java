package com.hao.backend.model.dto.app;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 对话生成代码请求
 */
@Data
public class AppChatRequest {

    @Schema(type = "string")
    @NotNull(message = "应用id不能为空")
    private Long appId;

    @NotBlank(message = "消息不能为空")
    @Size(max = 10000, message = "消息长度不能超过 10000")
    private String message;
}
