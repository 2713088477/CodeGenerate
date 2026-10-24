package com.hao.backend.security;

import com.hao.backend.model.entity.User;
import com.hao.backend.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DbUserDetailsService implements UserDetailsService {

    private final UserService userService;

    public DbUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String userAccount) {
        User user = userService.getOne(QueryWrapper.create().eq(User::getUserAccount, userAccount));
        if (user == null) {
            throw new UsernameNotFoundException("账号或密码错误");
        }
        return new LoginUser(user);
    }
}
