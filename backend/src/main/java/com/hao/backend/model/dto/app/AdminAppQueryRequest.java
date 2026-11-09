package com.hao.backend.model.dto.app;

import com.hao.backend.common.PageQuery;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.hao.backend.model.enums.CodeGenTypeEnum;

/**
 * 管理员分页查询所有应用：可按创建者账号（模糊）、应用名称（模糊）、代码生成类型（精确）筛选
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AdminAppQueryRequest extends PageQuery {

    @Size(max = 32, message = "账号长度不能超过 32")
    private String userAccount;

    @Size(max = 256, message = "应用名称长度不能超过 256")
    private String appName;

    /**
     * 代码生成类型，取值见 {@link CodeGenTypeEnum} 的 value
     */
    @Size(max = 64, message = "代码生成类型长度不能超过 64")
    private String codeGenType;
}
