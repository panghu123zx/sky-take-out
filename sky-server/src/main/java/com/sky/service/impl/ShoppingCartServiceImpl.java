package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.beans.beancontext.BeanContext;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    /**
     * 添加到购物车
     * @param shoppingCartDTO
     */
    @Override
    public void shopcartInsert(ShoppingCartDTO shoppingCartDTO) {
        //判断当前添加的商品是否存在
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        //只会查出一条数据
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        //如果商品已经存在，只需要让数量+1
        if (list != null && list.size() > 0) {
            ShoppingCart shoppingCartList = list.get(0);
            shoppingCartList.setNumber(shoppingCartList.getNumber() + 1);
            shoppingCartMapper.updateByNumber(shoppingCartList);
        } else {
            //不存在时插入数据需要判断属时菜品还是套餐

            Long dishId = shoppingCart.getDishId();
            if (dishId != null) {
                //此时是添加的菜品，需要添加菜品相关的属性
                Dish dish = dishMapper.selectById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
            } else {
                //此时添加的是套餐,需要添加套餐相关的属性
                Long setmealId = shoppingCart.getSetmealId();
                Setmeal setmeal = setmealMapper.selectByid(setmealId);
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            //添加数据
            shoppingCartMapper.shopcartInsert(shoppingCart);
        }
    }

    /**
     * 查看购物车
     */
    @Override
    public List<ShoppingCart> selectByuserId() {
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(userId)
                .build();
        List<ShoppingCart> cart= shoppingCartMapper.list(shoppingCart);
        return  cart;
    }

    /**
     *
     */
    @Override
    public void cleanShoppingCart() {
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.cleanShoppingCart(userId);
    }

    /**
     * 删除购物车的一个商品
     * @param shoppingCartDTO
     */
    @Override
    public void deleteShoppingCartOne(ShoppingCartDTO shoppingCartDTO) {
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart=new ShoppingCart();
        shoppingCart.setUserId(userId);
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);


        shoppingCartMapper.deleteOne(list.get(0));
    }
}
