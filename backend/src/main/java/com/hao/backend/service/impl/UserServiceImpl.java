package com.hao.backend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.hao.backend.model.entity.User;
import com.hao.backend.mapper.UserMapper;
import com.hao.backend.service.UserService;
import org.springframework.stereotype.Service;

/**
 * 用户表 服务层实现。
 *
 * @author <a href="https://github.com/2713088477">艺艺生辉</a>
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>  implements UserService{

}
