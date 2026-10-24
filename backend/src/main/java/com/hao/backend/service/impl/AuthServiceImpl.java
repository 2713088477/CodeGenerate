package com.hao.backend.service.impl;

import com.hao.backend.common.BusinessException;
import com.hao.backend.common.ResponseCode;
import com.hao.backend.model.dto.LoginRequest;
import com.hao.backend.model.dto.RegisterRequest;
import com.hao.backend.model.vo.UserProfileVO;
import com.hao.backend.model.entity.User;
import com.hao.backend.model.enums.UserRole;
import com.hao.backend.security.LoginUser;
import com.hao.backend.service.AuthService;
import com.hao.backend.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public AuthServiceImpl(UserService userService, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public UserProfileVO register(RegisterRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        String userAccount = request.getUserAccount().trim();
        // 1.校验用户是否存在
        if (userService.exists(QueryWrapper.create().eq(User::getUserAccount, userAccount))) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR, "账号已存在");
        }
        // 2.插入数据库
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(passwordEncoder.encode(request.getUserPassword()));
        user.setRole(UserRole.USER.getValue());
        try {
            userService.save(user);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR, "账号已存在");
        }
        // 3.存入session，返回用户信息
        return establishSession(userAccount, request.getUserPassword(), httpRequest, httpResponse);
    }

    @Override
    public UserProfileVO login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        return establishSession(request.getUserAccount().trim(), request.getUserPassword(), httpRequest, httpResponse);
    }

    private UserProfileVO establishSession(String userAccount, String rawPassword, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userAccount, rawPassword));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        return UserProfileVO.from(loginUser.getUser());
    }
}
