package com.hao.backend.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "账号不能为空")
    @Size(max = 32, message = "账号长度不能超过 32")
    private String userAccount;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度为 6 到 64 位")
    private String userPassword;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    @JsonIgnore
    @AssertTrue(message = "两次密码不一致")
    public boolean isConfirmPasswordMatched() {
        return userPassword != null && userPassword.equals(confirmPassword);
    }
}
