package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;

public interface UserService {
    /**
     * 用户的登入
     * @param userLoginDTO
     * @return
     */
    User userLogin(UserLoginDTO userLoginDTO);
}
