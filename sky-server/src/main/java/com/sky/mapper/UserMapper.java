package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface UserMapper {

    /**
     * 根据openid判断 user数据表中是否这个数据
     * @param openid
     * @return
     */
    @Select("select * from user where openid=#{openid}")
    User selectByOpenid(String openid);


    void insert(User user);

    @Select("select * from user where id=#{id}")
    User getById(Long userId);

    /**
     * 新增用户的统计
     * @param beginTime
     * @param endTime
     * @return
     */
    Integer getNewUser(LocalDateTime beginTime, LocalDateTime endTime);
}
