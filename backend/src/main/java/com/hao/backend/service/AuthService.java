package com.hao.backend.service;

import com.hao.backend.model.dto.auth.LoginRequest;
import com.hao.backend.model.dto.auth.RegisterRequest;
import com.hao.backend.model.entity.User;
import com.hao.backend.model.vo.UserProfileVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    UserProfileVO register(RegisterRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse);

    UserProfileVO login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse);

    void logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse);

    /**
     * 获取当前登录用户（从数据库查最新数据）
     * @throws com.hao.backend.common.BusinessException 未登录或用户已不存在时抛出 UNAUTHORIZED
     */
    User getCurrentUser();
}
