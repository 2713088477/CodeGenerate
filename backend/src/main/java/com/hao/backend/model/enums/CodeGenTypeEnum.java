package com.hao.backend.model.enums;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor 
public enum CodeGenTypeEnum {
    HTML("原生HTML模式", "html"),
    MULTI_FILE("原生多文件模式", "multi_file");

    private final String text;
    private final String value;

    public static CodeGenTypeEnum fromValue(String value) {
        return Arrays.stream(values())
                .filter(type -> type.value.equals(value))
                .findFirst()
                .orElse(null);
    }

}
