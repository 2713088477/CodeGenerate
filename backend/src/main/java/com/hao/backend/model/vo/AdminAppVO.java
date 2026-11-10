package com.hao.backend.model.vo;

import java.time.LocalDateTime;

import org.springframework.beans.BeanUtils;

import com.hao.backend.constant.AppPriority;
import com.hao.backend.model.entity.App;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理员查看应用时的返回结果，比 {@link AppVO} 多了优先级、是否精选和创建者信息
 */
@Data
public class AdminAppVO {

    @Schema(type = "string")
    private Long id;
    private String appName;
    private String cover;
    private String initPrompt;
    private String codeGenType;
    private String deployKey;
    private LocalDateTime deployedTime;
    private Integer priority;
    /**
     * 是否精选应用，由 priority 推导
     */
    private Boolean featured;

    @Schema(type = "string")
    private Long userId;
    /**
     * 创建者账号，创建者已被删除时为 null
     */
    private String userAccount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AdminAppVO from(App app, String userAccount) {
        AdminAppVO vo = new AdminAppVO();
        BeanUtils.copyProperties(app, vo);
        vo.setFeatured(AppPriority.isFeatured(app.getPriority()));
        vo.setUserAccount(userAccount);
        return vo;
    }
}
