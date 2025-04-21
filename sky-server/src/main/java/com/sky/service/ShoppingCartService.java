package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {
    /**
     * 添加到购物车
     * @param shoppingCartDTO
     */
    void shopcartInsert(ShoppingCartDTO shoppingCartDTO);

    /**
     * 查看购物车数据
     */
    List<ShoppingCart> selectByuserId();

    /**
     * 清空购物车
     */
    void cleanShoppingCart();

    /**
     * 删除购物车的一个商品
     * @param shoppingCartDTO
     */
    void deleteShoppingCartOne(ShoppingCartDTO shoppingCartDTO);
}
