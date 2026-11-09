package com.hao.backend.model.dto.app;

import com.hao.backend.common.PageQuery;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户查询自己的应用：可按应用名称（模糊）、部署标识（精确）筛选
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AppQueryRequest extends PageQuery {

    @Size(max = 256, message = "应用名称长度不能超过 256")
    private String appName;

    @Size(max = 64, message = "部署标识长度不能超过 64")
    private String deployKey;
}
