package com.hao.backend.model.vo;

import com.hao.backend.model.entity.User;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

@Data
public class AdminUserVO {

    private Long id;
    private String userAccount;
    private String username;
    private String avatar;
    private String bio;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AdminUserVO from(User user) {
        AdminUserVO response = new AdminUserVO();
        BeanUtils.copyProperties(user, response);
        return response;
    }
}
