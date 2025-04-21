package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderDetailMapper {
    /**
     * 插入订单详情的数据
     * @param orderDetailList
     */
    void insert(List<OrderDetail> orderDetailList);

}
