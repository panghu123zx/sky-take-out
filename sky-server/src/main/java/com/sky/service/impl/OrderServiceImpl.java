package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.WebSocketServer.WebSocketServer;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private WebSocketServer webSocketServer;
    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @Transactional
    @Override
    public OrderSubmitVO userSubmitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //处理地址和购物车的异常信息
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if(addressBook==null){
            //购物车的数据异常
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        //查询购物车中是否含有菜品
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart=new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if(list==null || list.size()==0){
            //购物车中的数据为空的异常
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }


        //向订单表中插入数据
        Orders orders=new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO,orders);
        orders.setNumber(String.valueOf(System.currentTimeMillis())); //订单号
        orders.setStatus(Orders.PENDING_PAYMENT); //订单状态
        orders.setPayStatus(Orders.UN_PAID); //支付状态
        orders.setUserId(userId); //用户ID
        orders.setOrderTime(LocalDateTime.now()); //下单时间
        orders.setPhone(addressBook.getPhone()); //电话
        orders.setConsignee(addressBook.getConsignee()); //收货人
        String address= addressBook.getProvinceName()  +"-"+addressBook.getCityName()+"-"+addressBook.getDistrictName();
        orders.setAddress(address); //地址信息

        orderMapper.insert(orders);

        //插入n条订单明细数据
        List<OrderDetail> orderDetailList=new ArrayList<>();
        for (ShoppingCart cart : list) {
            OrderDetail orderDetail=new OrderDetail();
            BeanUtils.copyProperties(cart,orderDetail);
            //设置订单的id
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insert(orderDetailList);

        //清空购物车
        shoppingCartMapper.cleanShoppingCart(userId);

        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderAmount(orders.getAmount())
                .orderNumber(orders.getNumber())
                .orderTime(orders.getOrderTime())
                .build();

        return orderSubmitVO;
    }


    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
//
//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }

        JSONObject jsonObject=new JSONObject();
        jsonObject.put("code","ORDERPAID");
        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        // 替代微信支付成功后的数据库订单状态更新，直接在这里更新了
        // 根据订单号查询当前用户的该订单
        Orders ordersDB = orderMapper.getByNumberAndUserId(ordersPaymentDTO.getOrderNumber(), userId);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders=new Orders();
        orders.setId(ordersDB.getId());
        orders.setStatus(Orders.TO_BE_CONFIRMED);
        orders.setPayStatus(Orders.PAID);
        orders.setCheckoutTime(LocalDateTime.now());


        orderMapper.update(orders);

        //用户下单时的来单提醒
        Map<String, Object> map=new HashMap<>();
        map.put("type",1); //消息类型，1：来单提醒， 2：客户催单
        map.put("orderId",ordersDB.getId());
        map.put("content",ordersDB.getNumber());  //返回订单号

        //发送给客户端
        String message = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(message);


        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    //    public void paySuccess(String outTradeNo) {
//
//
//        // 根据订单号查询订单
//        Orders ordersDB = orderMapper.getByNumber(outTradeNo);
//
//        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
//        Orders orders = Orders.builder()
//                .id(ordersDB.getId())
//                .status(Orders.TO_BE_CONFIRMED)
//                .payStatus(Orders.PAID)
//                .checkoutTime(LocalDateTime.now())
//                .build();
//
//        orderMapper.update(orders);
//    }


    /**
     * 历史订单的查询
     * @param page
     * @param pageSize
     * @param status
     * @return
     */
    @Override
    public PageResult historyOrder(Integer page, Integer pageSize, Integer status) {
        PageHelper.startPage(page,pageSize);
        OrdersPageQueryDTO pageQueryDTO=new OrdersPageQueryDTO();
        pageQueryDTO.setUserId(BaseContext.getCurrentId());
        pageQueryDTO.setStatus(status);

        Page<Orders> pages=orderMapper.getHistory(pageQueryDTO);

        List<OrderVO> orderVO=new ArrayList<>();
        if (pages!=null && pages.getTotal()>0){
            for (Orders orders : pages) {
                Long ordersId = orders.getId();
                List<OrderDetail> orderDetailList=orderMapper.selectByOrderId(ordersId);
                OrderVO vo=new OrderVO();
                BeanUtils.copyProperties(orders,vo);
                vo.setOrderDetailList(orderDetailList);
                orderVO.add(vo);
            }
        }
        return new PageResult(pages.getTotal(),orderVO);
    }

    /**
     * 查询订单详情
     * @param id
     * @return
     */
    @Override
    public OrderVO orderDetail(Long id) {
        Orders order= orderMapper.selectById(id);
        Long orderId=id;
        List<OrderDetail> orderDetailList = orderMapper.selectByOrderId(orderId);
        OrderVO orderVO=new OrderVO();
        BeanUtils.copyProperties(order,orderVO);
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;
    }

    /**
     * 取消订单
     * @param id
     */
    @Override
    @Transactional
    public void cancelorder(Long id) {
        //不可以删除订单，只能修改订单的状态和支付的状态
        Orders orders = orderMapper.selectById(id);
        if(orders==null){
            throw  new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //查看订单状态,大于2就是没有支付的状态，不可以删除
        if(orders.getStatus()>2){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //接单状态下时，取消订单，需要退款
        if(orders.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            //微信退款的代码
            //将支付状态改为退款
            orders.setPayStatus(Orders.REFUND);
        }

        //更新订单状态，取消原因，取消时间
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 再来一单
     * @param id
     */
    @Override
    public void orderAgain(Long id) {
        //需要重新加载到购物车中
        Long orderId=id;
        List<OrderDetail> orderDetailList = orderMapper.selectByOrderId(orderId);
        Long userId = BaseContext.getCurrentId();
        for (OrderDetail orderDetail : orderDetailList) {
            ShoppingCart shoppingCart=new ShoppingCart();
            BeanUtils.copyProperties(orderDetail,shoppingCart);
            shoppingCart.setUserId(userId);
            shoppingCartMapper.shopcartInsert(shoppingCart);
        }
    }

    /**
     * 订单搜索
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult orderSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.getHistory(ordersPageQueryDTO);
        List<OrderVO> orderVOS=new ArrayList<>();
        if(page!=null && page.getTotal()>0){
            for (Orders orders : page) {
                OrderVO orderVO=new OrderVO();
                BeanUtils.copyProperties(orders,orderVO);
                Long ordersId = orders.getId();
                List<OrderDetail> orderDetailList = orderMapper.selectByOrderId(ordersId);

                String orderDishes = orderDishes(orderDetailList);

                orderVO.setOrderDishes(orderDishes);
                orderVO.setOrderDetailList(orderDetailList);
                orderVOS.add(orderVO);
            }
        }
        return new PageResult(page.getTotal(),orderVOS);
    }

    //获取订单时的菜品
    private String orderDishes(List<OrderDetail> orderDetailList){
        //每一条菜品都拼接到一起，例如：菜品*3
        StringBuffer sb=new StringBuffer();
        for (OrderDetail orderDetail : orderDetailList) {
            String name = orderDetail.getName();
            Integer number = orderDetail.getNumber();
            String s=name+"*"+number+";";
            sb.append(s);
        }
        return sb.toString();
    }

    /**
     * 各个状态的订单数量统计
     * @return
     */
    @Override
    public OrderStatisticsVO statusCount() {
        OrderStatisticsVO orderStatisticsVO=new OrderStatisticsVO();
        //待派送的数量
        Integer confirmNum=orderMapper.getNumByConfirm();
        //派送中的数量
        Integer deliveryNum=orderMapper.getNumByDelivery();
        //待接单的数量
        Integer toBeConfirm=orderMapper.getNumByToBeConfirm();

        orderStatisticsVO.setConfirmed(confirmNum);
        orderStatisticsVO.setToBeConfirmed(toBeConfirm);
        orderStatisticsVO.setDeliveryInProgress(deliveryNum);
        return orderStatisticsVO;
    }

    /**
     * 商家接单
     * @param ordersConfirmDTO
     */
    @Override
    public void orderConfirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders=new Orders();
        orders.setId(ordersConfirmDTO.getId());
        orders.setStatus(Orders.CONFIRMED);

        orderMapper.update(orders);
    }

    /**
     * 商家拒单
     */
    @Override
    @Transactional
    public void rejectOrder(OrdersRejectionDTO ordersRejectionDTO) {
        Orders ordersDB = orderMapper.selectById(ordersRejectionDTO.getId());
        //不是待接单的状态，不能拒单
        if(!ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }


        Orders orders = Orders.builder()
                .id(ordersRejectionDTO.getId())
                .status(Orders.CANCELLED) //拒单状态
                .rejectionReason(ordersRejectionDTO.getRejectionReason()) //拒单原因
                .cancelTime(LocalDateTime.now()) //取消的时间
                .build();
        //通过传递的id判断pay_status 判断用户是否付款，付款了之后就要退款
        orderMapper.update(orders);
    }

    /**
     * 商家取消订单
     * @param ordersCancelDTO
     */
    @Override
    public void adminCancelOrder(OrdersCancelDTO ordersCancelDTO) {
        Orders ordersDB = orderMapper.selectById(ordersCancelDTO.getId());

        if(ordersDB.getPayStatus()==1) {
            //需要给用户进行退款
        }
        Orders orders = Orders.builder()
                .id(ordersCancelDTO.getId())
                .status(Orders.CANCELLED)
                .cancelTime(LocalDateTime.now())
                .cancelReason(ordersCancelDTO.getCancelReason())
                .build();
        orderMapper.update(orders);
    }


    /**
     * 派送订单
     * @param id
     */
    @Override
    @Transactional
    public void deliveryOrder(Long id) {
        Orders ordersDB = orderMapper.selectById(id);
        //不是派送中的订单无法派送
        if(!ordersDB.getStatus().equals(Orders.CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders=new Orders();
        orders.setId(id);
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);

        orderMapper.update(orders);
    }

    /**
     * 完成订单
     * @param id
     */
    @Override
    @Transactional
    public void completeOrder(Long id) {
        Orders ordersDB = orderMapper.selectById(id);

        //不是派送中的状态，无法完成订单
        if(ordersDB==null || !ordersDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders=new Orders();
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now()); //送达时间
        orders.setId(id);

        orderMapper.update(orders);
    }

    /**
     * 客户催单
     * @param id
     */
    @Override
    public void reminderOrder(Long id) {
        Orders orders = orderMapper.selectById(id);

        //判断订单是否存在
        if(orders==null){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Map<String, Object> map=new HashMap<>();
        map.put("type",2);
        map.put("orderId",id);
        map.put("content",orders.getNumber());

        //实现客户催单
        String message = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(message);
    }
}
