package com.codehev.api_interface.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.codehev.api_interface.model.entity.User;
import com.codehev.api_interface.service.UserService;
import com.codehev.api_interface.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
* @author codeh
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-01-19 17:40:42
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

}




