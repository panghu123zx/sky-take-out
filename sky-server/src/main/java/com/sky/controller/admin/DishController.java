package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@Slf4j
@RequestMapping("/admin/dish")
@Api(tags = "菜品管理")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result DishInsert(@RequestBody DishDTO dishDTO){
        log.info("新增菜品...");
        dishService.dishInsert(dishDTO);
        //新增菜品时会影响到数据库的变化，导致redis不准确, 因而需要删除掉redis中的方法，然后重新查询
        String key="dish_"+dishDTO.getCategoryId();
        cleancache(key);
        return Result.success();
    }


    /**
     * 菜品的分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("菜品的分页查询")
    public Result<PageResult> selectPage(DishPageQueryDTO dishPageQueryDTO){
        log.info("分页查询...");
        PageResult page= dishService.selectPage(dishPageQueryDTO);
        return Result.success(page);
    }

    /**
     * 批量删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("批量删除菜品管理")
    public Result deleteByIds(@RequestParam List<Long> ids){
        log.info("批量删除菜品管理...");
        dishService.deleteByIds(ids);

        //批量删除时，会导致多个数据库发生变化，所以把redis的缓存数据全部删除，然后在重新加载
        redisTemplate.delete("dish_*");
        return Result.success();
    }

    /**
     * 根据id查询菜品,用于菜品的回显
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<DishVO> selectById(@PathVariable Long id){
        log.info("根据id查询菜品...");
        DishVO dishVO= dishService.selectById(id);
        return Result.success(dishVO);
    }

    /**
     * 修改菜品
     * @param dishDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改菜品")
    public Result updateDish(@RequestBody DishDTO dishDTO){
        log.info("修改菜品...");
        dishService.updateDish(dishDTO);

        //修改菜品还有可能涉及到菜品的分类，操作了多个数据库
        redisTemplate.delete("dish_*");

        return Result.success();
    }

    /**
     * 修改菜品的状态
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("修改菜品的状态")
    public Result updateStatus(@PathVariable Integer status,Long id){
        log.info("修改菜品的状态...");
        dishService.updateStatus(status,id);
        //修改这一个菜品，还需要查询数据库，不如直接重新加载redis

        cleancache("dish_*");
        return Result.success();
    }

    @ApiOperation("根据分类id查询菜品数据")
    @GetMapping("/list")
    public Result<List<Dish>> selectByCategoryId(Integer categoryId){
        log.info("根据分类id查询菜品数据...");
        List<Dish> dishes= dishService.selectByCategoryId(categoryId);
        return Result.success(dishes);
    }

    private void cleancache(String patten){
        Set keys = redisTemplate.keys(patten);
        redisTemplate.delete(keys);
    }
}
