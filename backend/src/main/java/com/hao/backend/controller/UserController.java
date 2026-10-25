package com.hao.backend.controller;

import com.hao.backend.common.BaseResponse;
import com.hao.backend.common.PageResult;
import com.hao.backend.model.dto.AdminUserCreateRequest;
import com.hao.backend.model.dto.AdminUserUpdateRequest;
import com.hao.backend.model.dto.UserQueryRequest;
import com.hao.backend.model.vo.AdminUserVO;
import com.hao.backend.service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    @GetMapping("/page")
    public BaseResponse<PageResult<AdminUserVO>> page(@Valid UserQueryRequest query) {
        return BaseResponse.success(userService.pageUsers(query));
    }

    @PostMapping
    public BaseResponse<AdminUserVO> create(@RequestBody @Valid AdminUserCreateRequest request) {
        return BaseResponse.success(userService.createUser(request));
    }

    @PutMapping("/{id}")
    public BaseResponse<AdminUserVO> update(
            @Parameter(schema = @Schema(type = "string")) @PathVariable Long id,
            @RequestBody @Valid AdminUserUpdateRequest request) {
        return BaseResponse.success(userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    public BaseResponse<Void> delete(@Parameter(schema = @Schema(type = "string")) @PathVariable Long id) {
        userService.deleteUser(id);
        return BaseResponse.success();
    }
}
