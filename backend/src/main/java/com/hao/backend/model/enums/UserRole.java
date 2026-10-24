package com.hao.backend.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum UserRole {

    ADMIN("管理员", "admin"),
    USER("用户", "user");

    private final String text;
    private final String value;

    public static UserRole fromValue(String value) {
        return Arrays.stream(values())
                .filter(role -> role.value.equals(value))
                .findFirst()
                .orElse(null);
    }
}
