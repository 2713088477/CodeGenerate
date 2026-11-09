package com.hao.backend.model.vo;

import java.time.LocalDateTime;

import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hao.backend.model.entity.App;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AppVO {

    @Schema(type = "string")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;
    private String appName;
    private String cover;
    private String initPrompt;
    private String codeGenType;
    private String deployKey;
    private LocalDateTime deployedTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AppVO from(App app) {
        AppVO vo = new AppVO();
        BeanUtils.copyProperties(app, vo);
        return vo;
    }
}
