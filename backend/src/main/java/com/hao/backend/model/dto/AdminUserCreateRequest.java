package com.hao.backend.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminUserCreateRequest {

    @NotBlank(message = "账号不能为空")
    @Size(max = 32, message = "账号长度不能超过 32")
    private String userAccount;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度为 6 到 64 位")
    private String userPassword;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    @Size(max = 32, message = "用户名长度不能超过 32")
    private String username;

    @Size(max = 1024, message = "头像地址长度不能超过 1024")
    private String avatar;

    @Size(max = 512, message = "个人简介长度不能超过 512")
    private String bio;

    private String role;

    @JsonIgnore
    @AssertTrue(message = "两次密码不一致")
    public boolean isConfirmPasswordMatched() {
        return userPassword != null && userPassword.equals(confirmPassword);
    }
}
