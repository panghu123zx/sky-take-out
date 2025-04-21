package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminOrderController")
@Slf4j
@Api(tags = "订单管理模块")
@RequestMapping("/admin/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    /**
     * 订单搜索
     * @param ordersPageQueryDTO
     * @return
     */
    @ApiOperation("订单搜索")
    @GetMapping("/conditionSearch")
    public Result<PageResult> OrderSearch(OrdersPageQueryDTO ordersPageQueryDTO){
        log.info("订单搜索...");
        PageResult page= orderService.orderSearch(ordersPageQueryDTO);
        return Result.success(page);
    }


    /**
     * 各个状态的订单数量统计
     * @return
     */
    @GetMapping("/statistics")
    @ApiOperation("各个状态的订单数量统计")
    public Result<OrderStatisticsVO> statusCount(){
        log.info("各个状态的订单数量统计...");
        OrderStatisticsVO orderStatisticsVO=orderService.statusCount();
        return Result.success(orderStatisticsVO);
    }

    /**
     * 查询订单详情
     * @param id
     * @return
     */
    @ApiOperation("查询订单详情")
    @GetMapping("/details/{id}")
    public Result<OrderVO> orderDetail(@PathVariable Long id){
        log.info("查询订单详情...");
        OrderVO orderVO = orderService.orderDetail(id);
        return Result.success(orderVO);
    }


    /**
     * 商家接单
     * @param ordersConfirmDTO
     * @return
     */
    @ApiOperation("商家接单")
    @PutMapping("/confirm")
    public Result  orderConfirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO){

        log.info("商家接单...");
        orderService.orderConfirm(ordersConfirmDTO);
        return Result.success();
    }

    /**
     * 商家拒单
     * @param ordersRejectionDTO
     * @return
     */
    @ApiOperation("商家拒单")
    @PutMapping("/rejection")
    public Result rejectOrder(@RequestBody OrdersRejectionDTO ordersRejectionDTO){
        log.info("商家拒单...");
        orderService.rejectOrder(ordersRejectionDTO);
        return Result.success();
    }

    /**
     * 商家取消订单
     * @param ordersCancelDTO
     * @return
     */
    @ApiOperation("商家取消订单")
    @PutMapping("/cancel")
    public Result cancelOrder(@RequestBody OrdersCancelDTO ordersCancelDTO){
        log.info("商家取消订单...");
        orderService.adminCancelOrder(ordersCancelDTO);
        return Result.success();
    }

    /**
     * 派送订单
     * @param id
     * @return
     */
    @ApiOperation("派送订单")
    @PutMapping("/delivery/{id}")
    private Result orderDelivery(@PathVariable Long id){
        log.info("派送订单...");
        orderService.deliveryOrder(id);
        return Result.success();
    }

    /**
     * 完成订单
     * @return
     */
    @ApiOperation("完成订单")
    @PutMapping("/complete/{id}")
    public Result completeOrder(@PathVariable Long id){
        log.info("完成订单....");

        orderService.completeOrder(id);
        return Result.success();
    }
}
