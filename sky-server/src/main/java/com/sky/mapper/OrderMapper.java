package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    /**
     * 订单数据的插入
     * @param orders
     */
     void insert(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);


    @Select("select * from orders where number = #{orderNumber} and user_id= #{userId}")
    Orders getByNumberAndUserId(String orderNumber, Long userId);


    /**
     * 历史订单的查询
     * @param pageQueryDTO
     * @return
     */
    Page<Orders> getHistory(OrdersPageQueryDTO pageQueryDTO);

    /**
     * 查询历史订单的菜品信息
     * @param orderId
     * @return
     */
    @Select("select * from order_detail where order_id=#{orderId}")
    List<OrderDetail> selectByOrderId(Long orderId);

    /**
     * 查询订单详情
     * @param id
     * @return
     */
    @Select("select * from orders where id=#{id}")
    Orders selectById(Long id);

    //待派送的数量
    @Select("select count(*) from orders where status=3")
    Integer getNumByConfirm();

    //派送中的数量
    @Select("select count(*) from orders where status=4")
    Integer getNumByDelivery();

    //待接单的数量
    @Select("select count(*) from orders where status=2")
    Integer getNumByToBeConfirm();

    /**
     * 观察订单是否超时
     * @param status
     * @param orderTime
     */
    @Select("select * from orders where status=#{status} and order_time<#{orderTime}")
    List<Orders> selectStatusAndOrderTime(Integer status, LocalDateTime orderTime);

    /**
     * 营业额统计
     * @param map
     */
    Double getTurnoverStatistics(Map<String, Object> map);

    /**
     * 有效订单数
     * @param map
     */
    Integer getOrderStatistics(Map<String, Object> map);

    /**
     * 查询销量排名top10接口
     * @param beginTime
     * @param endTime
     * @return
     */
    List<GoodsSalesDTO> getSaleTop10(LocalDateTime beginTime, LocalDateTime endTime);
}
