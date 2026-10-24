package com.hao.backend.model.vo;

import com.hao.backend.model.entity.User;
import lombok.Data;

@Data
public class UserProfileVO {

    private Long id;
    private String userAccount;
    private String username;
    private String role;

    public static UserProfileVO from(User user) {
        UserProfileVO response = new UserProfileVO();
        response.setId(user.getId());
        response.setUserAccount(user.getUserAccount());
        response.setUsername(user.getUsername());
        response.setRole(user.getRole());
        return response;
    }
}
