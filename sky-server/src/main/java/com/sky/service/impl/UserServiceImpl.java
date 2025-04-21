package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;
    private static final String url="https://api.weixin.qq.com/sns/jscode2session";
    @Override
    public User userLogin(UserLoginDTO userLoginDTO) {
        //使用微信登入时 传递的属性值
        Map<String,String> map=new HashMap<>();
        map.put("appid",weChatProperties.getAppid());
        map.put("secret",weChatProperties.getSecret());
        map.put("js_code",userLoginDTO.getCode());
        map.put("grant_type","authorization_code");
        String json = HttpClientUtil.doGet(url, map);

        //获取到其中的openid
        JSONObject jsonObject= JSON.parseObject(json);
        String openid = jsonObject.getString("openid");

        //根据openid判断用户是否存在
        if(openid==null){
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        //根据openid判断给用户是否为新用户
        User user=userMapper.selectByOpenid(openid);

        //表示不是新用户，自动注册
        if(user==null){
            user= User.builder()
                    .createTime(LocalDateTime.now())
                    .openid(openid)
                    .build();
            //在controller中会使用到用户的id，需要返回
            userMapper.insert(user);
        }

        return user;
    }
}
