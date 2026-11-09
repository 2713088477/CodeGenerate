package com.hao.backend.model.dto.app;

import com.hao.backend.constant.AppPriority;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理员修改应用请求：可修改应用名称、优先级，字段为 null 表示不修改，至少传一项。
 * 优先级数值越小越靠前，小于等于 {@link AppPriority#FEATURED_MAX} 为精选应用。
 */
@Data
public class AdminAppUpdateRequest {

    @Size(max = 256, message = "应用名称长度不能超过 256")
    private String appName;

    @Min(value = AppPriority.MIN, message = "优先级不能小于 " + AppPriority.MIN)
    @Max(value = AppPriority.MAX, message = "优先级不能大于 " + AppPriority.MAX)
    private Integer priority;
}
