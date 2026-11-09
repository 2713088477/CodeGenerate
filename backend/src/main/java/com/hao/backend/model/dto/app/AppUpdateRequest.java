package com.hao.backend.model.dto.app;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class AppUpdateRequest {

    @NotBlank(message = "应用名称不能为空")
    @Size(max = 256, message = "应用名称长度不能超过 256")
    private String appName;
}
