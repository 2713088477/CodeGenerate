package com.hao.backend.controller;

import com.hao.backend.common.BaseResponse;
import com.hao.backend.model.dto.LoginRequest;
import com.hao.backend.model.dto.RegisterRequest;
import com.hao.backend.model.vo.UserProfileVO;
import com.hao.backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public BaseResponse<UserProfileVO> register(@RequestBody @Valid RegisterRequest request,
                                                HttpServletRequest httpRequest,
                                                HttpServletResponse httpResponse) {
        return BaseResponse.success(authService.register(request, httpRequest, httpResponse));
    }

    @PostMapping("/login")
    public BaseResponse<UserProfileVO> login(@RequestBody @Valid LoginRequest request,
                                             HttpServletRequest httpRequest,
                                             HttpServletResponse httpResponse) {
        return BaseResponse.success(authService.login(request, httpRequest, httpResponse));
    }

    @PostMapping("/logout")
    public BaseResponse<Void> logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        authService.logout(httpRequest, httpResponse);
        return BaseResponse.success();
    }
}
