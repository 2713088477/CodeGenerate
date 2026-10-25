package com.hao.backend.service.impl;

import com.hao.backend.common.BusinessException;
import com.hao.backend.common.PageResult;
import com.hao.backend.common.ResponseCode;
import com.hao.backend.mapper.UserMapper;
import com.hao.backend.model.dto.AdminUserCreateRequest;
import com.hao.backend.model.dto.AdminUserUpdateRequest;
import com.hao.backend.model.dto.UserQueryRequest;
import com.hao.backend.model.entity.User;
import com.hao.backend.model.enums.UserRole;
import com.hao.backend.model.vo.AdminUserVO;
import com.hao.backend.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.core.util.LambdaGetter;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 用户表 服务层实现。
 *
 * @author <a href="https://github.com/2713088477">艺艺生辉</a>
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    private static final Map<String, LambdaGetter<User>> SORT_FIELDS = Map.of(
            "id", User::getId,
            "userAccount", User::getUserAccount,
            "username", User::getUsername,
            "role", User::getRole,
            "createdAt", User::getCreatedAt,
            "updatedAt", User::getUpdatedAt
    );

    @Override
    public PageResult<AdminUserVO> pageUsers(UserQueryRequest query) {
        String userAccount = StringUtils.trimToNull(query.getUserAccount());
        String username = StringUtils.trimToNull(query.getUsername());
        String role = StringUtils.trimToNull(query.getRole());
        if (role != null && UserRole.fromValue(role) == null) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR, "角色不正确");
        }

        QueryWrapper queryWrapper = QueryWrapper.create()
                .like(User::getUserAccount, userAccount, userAccount != null)
                .like(User::getUsername, username, username != null)
                .eq(User::getRole, role, role != null);
        applySort(queryWrapper, query);

        Page<User> page = this.page(new Page<>(query.getPageNum(), query.getPageSize()), queryWrapper);
        return PageResult.of(page, AdminUserVO::from);
    }

    @Override
    public AdminUserVO createUser(AdminUserCreateRequest request) {
        String userAccount = request.getUserAccount().trim();
        if (exists(QueryWrapper.create().eq(User::getUserAccount, userAccount))) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR, "账号已存在");
        }
        String role = StringUtils.trimToNull(request.getRole());
        if (role == null) {
            role = UserRole.USER.getValue();
        } else if (UserRole.fromValue(role) == null) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR, "角色不正确");
        }

        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(passwordEncoder.encode(request.getUserPassword()));
        user.setUsername(StringUtils.trimToNull(request.getUsername()));
        user.setAvatar(StringUtils.trimToNull(request.getAvatar()));
        user.setBio(StringUtils.trimToNull(request.getBio()));
        user.setRole(role);
        try {
            save(user);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR, "账号已存在");
        }
        return AdminUserVO.from(getExistingUser(user.getId()));
    }

    private void applySort(QueryWrapper queryWrapper, UserQueryRequest query) {
        String sortField = StringUtils.trimToNull(query.getSortField());
        if (sortField == null) {
            queryWrapper.orderBy(User::getCreatedAt, false);
            return;
        }
        LambdaGetter<User> column = SORT_FIELDS.get(sortField);
        if (column == null) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR, "排序字段不正确");
        }
        queryWrapper.orderBy(column, Boolean.TRUE.equals(query.getAsc()));
    }

    @Override
    public AdminUserVO updateUser(Long id, AdminUserUpdateRequest request) {
        User user = getExistingUser(id);
        String password = StringUtils.trimToNull(request.getUserPassword());
        String role = StringUtils.trimToNull(request.getRole());
        boolean changeUsername = request.getUsername() != null;
        boolean changeAvatar = request.getAvatar() != null;
        boolean changeBio = request.getBio() != null;
        boolean changeRole = request.getRole() != null;
        if (changeRole && UserRole.fromValue(role) == null) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR, "角色不正确");
        }
        if (!changeUsername && !changeAvatar && !changeBio && !changeRole && password == null) {
            return AdminUserVO.from(user);
        }

        String encodedPassword = password == null ? null : passwordEncoder.encode(password);
        UpdateChain.of(User.class)
                .set(User::getUsername, StringUtils.trimToNull(request.getUsername()), changeUsername)
                .set(User::getAvatar, StringUtils.trimToNull(request.getAvatar()), changeAvatar)
                .set(User::getBio, StringUtils.trimToNull(request.getBio()), changeBio)
                .set(User::getRole, role, changeRole)
                .set(User::getUserPassword, encodedPassword, encodedPassword != null)
                .where(User::getId).eq(id)
                .update();
        return AdminUserVO.from(getExistingUser(id));
    }

    @Override
    public void deleteUser(Long id) {
        getExistingUser(id);
        removeById(id);
    }

    private User getExistingUser(Long id) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException(ResponseCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }
}
