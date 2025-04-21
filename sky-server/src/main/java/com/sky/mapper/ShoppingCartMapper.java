package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {
    /**
     *  添加到购物车
     * @param shoppingCart
     */
    @Insert("insert into shopping_cart (name,image,user_id,dish_id,setmeal_id,dish_flavor,number,amount,create_time)" +
            "values " +
            "(#{name},#{image},#{userId},#{dishId},#{setmealId},#{dishFlavor},#{number},#{amount},#{createTime})")
     void shopcartInsert(ShoppingCart shoppingCart) ;

    /**
     * 查询购物车中是否含有当前菜品
     * @param shoppingCart
     * @return
     */
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    /**
     * 更新商品发数量
     * @param shoppingCart
     */
    @Update("update shopping_cart set number=#{number} where id=#{id}")
    void updateByNumber(ShoppingCart shoppingCart);

    /**
     * 清空购物车
     * @param userId
     */
    @Delete("delete  from shopping_cart where user_id=#{userId}")
    void cleanShoppingCart(Long userId);

    /**
     * 删除购物车的以一个元素
     * @param shoppingCart
     */
    void deleteOne(ShoppingCart shoppingCart);
}
