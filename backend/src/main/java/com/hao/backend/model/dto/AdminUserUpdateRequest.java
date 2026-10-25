package com.hao.backend.model.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminUserUpdateRequest {

    @Size(max = 32, message = "用户名长度不能超过 32")
    private String username;

    @Size(max = 1024, message = "头像地址长度不能超过 1024")
    private String avatar;

    @Size(max = 512, message = "个人简介长度不能超过 512")
    private String bio;

    private String role;

    @Size(min = 6, max = 64, message = "密码长度为 6 到 64 位")
    private String userPassword;
}
