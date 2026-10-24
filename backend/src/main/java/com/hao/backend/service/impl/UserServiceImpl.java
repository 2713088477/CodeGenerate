package com.hao.backend.service.impl;

import com.hao.backend.common.BusinessException;
import com.hao.backend.common.PageResult;
import com.hao.backend.common.ResponseCode;
import com.hao.backend.mapper.UserMapper;
import com.hao.backend.model.dto.UserQueryRequest;
import com.hao.backend.model.entity.User;
import com.hao.backend.model.enums.UserRole;
import com.hao.backend.model.vo.AdminUserVO;
import com.hao.backend.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.util.LambdaGetter;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 用户表 服务层实现。
 *
 * @author <a href="https://github.com/2713088477">艺艺生辉</a>
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

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
}
