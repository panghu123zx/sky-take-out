package com.sky.controller.admin;

import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@Api(tags = "套餐管理")
@RequestMapping("/admin/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    /**
     * 套餐管理的分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("套餐管理的分页查询")
    public Result<PageResult> selectPage(SetmealPageQueryDTO setmealPageQueryDTO){
        log.info("套餐管理的分页查询...");
        PageResult pageResult= setmealService.selectPage(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 新增套餐
     * @param setmealDTO
     * @return
     */
    @ApiOperation("新增套餐")
    @PostMapping
    @CacheEvict(cacheNames = "setmealcache",key = "#setmealDTO.categoryId") //清理缓存数据
    public Result insertSetmeal(@RequestBody SetmealDTO setmealDTO){
        log.info("新增套餐...");
        setmealService.insertSetmeal(setmealDTO);
        return Result.success();
    }


    /**
     * 批量删除套餐
     * @param ids
     * @return
     */
     @ApiOperation("批量删除套餐")
    @DeleteMapping
     @CacheEvict(cacheNames = "setmealcache",allEntries = true)  //批量删除时，没有categoryId所以全部删除
    public Result deleteByIds(@RequestParam List<Long> ids){
        log.info("批量删除套餐");
        setmealService.deleteByIds(ids);
        return Result.success();
    }

    /**
     * 修改套餐的状态
     * @param status
     * @param id
     * @return
     */
    @ApiOperation("修改套餐的状态")
    @PostMapping("/status/{status}")
    @CacheEvict(cacheNames = "setmealcache",allEntries = true)
    public Result updateStatus(@PathVariable Integer status,Long id){
         log.info("修改套餐的状态...");
         setmealService.updateStatus(status,id);
         return Result.success();
    }


    @ApiOperation("根据id查询套餐")
    @GetMapping("/{id}")
    public Result<SetmealVO> selectById(@PathVariable Long id){
        log.info("根据id查询套餐...");
        SetmealVO setmealVO= setmealService.selectById(id);
        return Result.success(setmealVO);
    }

    /**
     * 修改套餐信息
     * @param setmealDTO
     * @return
     */
    @ApiOperation("修改套餐信息")
    @PutMapping
    @CacheEvict(cacheNames = "setmealcache",allEntries = true)
    public Result updateSetmeal(@RequestBody SetmealDTO setmealDTO){
        log.info("修改套餐信息...");
        setmealService.updateSetmeal(setmealDTO);
        return Result.success();
    }
}
