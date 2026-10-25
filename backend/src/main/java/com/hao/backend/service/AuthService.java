package com.hao.backend.service;

import com.hao.backend.model.dto.LoginRequest;
import com.hao.backend.model.dto.RegisterRequest;
import com.hao.backend.model.vo.UserProfileVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    UserProfileVO register(RegisterRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse);

    UserProfileVO login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse);

    void logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse);
}
