package com.hao.backend.model.dto;

import com.hao.backend.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserQueryRequest extends PageQuery {

    private String userAccount;

    private String username;

    private String role;
}
