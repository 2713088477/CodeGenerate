package com.hao.backend.model.dto.app;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class AppCreateRequest {

    @NotBlank(message = "初始化提示词不能为空")
    @Size(max = 10000, message = "初始化提示词长度不能超过 10000")
    private String initPrompt;
}
