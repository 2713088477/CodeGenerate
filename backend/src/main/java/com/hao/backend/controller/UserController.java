package com.hao.backend.controller;

import com.hao.backend.common.BaseResponse;
import com.hao.backend.common.PageResult;
import com.hao.backend.model.dto.UserQueryRequest;
import com.hao.backend.model.vo.AdminUserVO;
import com.hao.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户表 控制层。
 *
 * @author <a href="https://github.com/2713088477">艺艺生辉</a>
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/page")
    public BaseResponse<PageResult<AdminUserVO>> page(@Valid UserQueryRequest query) {
        return BaseResponse.success(userService.pageUsers(query));
    }
}
