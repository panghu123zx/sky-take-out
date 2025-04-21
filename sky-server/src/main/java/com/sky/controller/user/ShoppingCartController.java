package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/user/shoppingCart")
@Api(tags = "c端-购物车接口")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加到购物车
     * @return
     */
    @ApiOperation("添加到购物车")
    @PostMapping("/add")
    public Result shopcartInsert(@RequestBody ShoppingCartDTO shoppingCartDTO){
        log.info("添加到购物车...");
        shoppingCartService.shopcartInsert(shoppingCartDTO);
        return Result.success();
    }

    /**
     * 查看购物车的数据
     * @return
     */
    @ApiOperation("查看购物车")
    @GetMapping("/list")
    public Result<List<ShoppingCart>> shoppingcartLook(){
        log.info("查看购物车...");
        //通过请求头的userId获取到是那个用户进行操作
        List<ShoppingCart>  cart=shoppingCartService.selectByuserId();
        return  Result.success(cart);
    }

    /**
     * 清空购物车
     */
    @DeleteMapping("/clean")
    @ApiOperation("清空购物车")
    public Result cleanShoppingCart(){
        log.info("清空购物车...");
        shoppingCartService.cleanShoppingCart();
        return Result.success();
    }

    /**
     * 删除购物车的一个商品
     * @return
     */
    @ApiOperation("删除购物车的一个商品")
    @PostMapping("/sub")
    public Result deleteShoppingCartOne(@RequestBody ShoppingCartDTO shoppingCartDTO){
        log.info("删除购物车的一个商品...");
        shoppingCartService.deleteShoppingCartOne(shoppingCartDTO);
        return Result.success();
    }
}
