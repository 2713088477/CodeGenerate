package com.hao.backend.service;

import com.hao.backend.common.PageResult;
import com.hao.backend.model.dto.AdminUserCreateRequest;
import com.hao.backend.model.dto.AdminUserUpdateRequest;
import com.hao.backend.model.dto.UserQueryRequest;
import com.hao.backend.model.entity.User;
import com.hao.backend.model.vo.AdminUserVO;
import com.mybatisflex.core.service.IService;

/**
 * 用户表 服务层。
 *
 * @author <a href="https://github.com/2713088477">艺艺生辉</a>
 */
public interface UserService extends IService<User> {

    PageResult<AdminUserVO> pageUsers(UserQueryRequest query);

    AdminUserVO createUser(AdminUserCreateRequest request);

    AdminUserVO updateUser(Long id, AdminUserUpdateRequest request);

    void deleteUser(Long id);
}
