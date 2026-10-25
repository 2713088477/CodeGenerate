package com.hao.backend.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hao.backend.model.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.beans.BeanUtils;

@Data
public class UserProfileVO {

    @Schema(type = "string")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;
    private String userAccount;
    private String username;
    private String avatar;
    private String role;

    public static UserProfileVO from(User user) {
        UserProfileVO response = new UserProfileVO();
        BeanUtils.copyProperties(user, response);
        return response;
    }
}
