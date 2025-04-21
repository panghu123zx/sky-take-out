package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;

    /**
     * 待付款15分钟内检查，是否完成付款
     */
    @Scheduled(cron = "0 * * * * ?")  //每分钟触发一次
    public void OrderTimeOut(){
        log.info("更新超时的订单...");
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15); //得到当前时间的15分钟之前
        //需要查询订单状态为 待付款状态下的，离当前时间还有15分钟的时间 ：下单的时间 < 当前时间 - 15 
        List<Orders> ordersList=orderMapper.selectStatusAndOrderTime(Orders.PENDING_PAYMENT,time);

        //更新订单超时的订单
        if(ordersList!=null && ordersList.size()>0){
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("订单超时，自动取消");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
            }
        }
    }

    /**
     * 每天的一点检查派送中的订单 ，将他完成
     */
    @Scheduled(cron = "0 0 1 * * ?") //每天一点检查订单
    public void finallyOrder(){
        log.info("完成订单...");

        LocalDateTime time = LocalDateTime.now().plusMinutes(-60); //一点触发时，检查前一个工作日的订单状况
        List<Orders> ordersList=orderMapper.selectStatusAndOrderTime(Orders.DELIVERY_IN_PROGRESS,time);
        if(ordersList!=null && ordersList.size()>0){
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }
    }
}
