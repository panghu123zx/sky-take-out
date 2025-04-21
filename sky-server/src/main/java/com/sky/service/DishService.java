package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {
    /**
     * 新增菜品
     * @param dishDTO
     */
    void dishInsert(DishDTO dishDTO);

    /**
     * 菜品的分页查询
     * @param dishPageQueryDTO
     * @return
     */
    PageResult selectPage(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 批量删除菜品管理
     * @param ids
     */
    void deleteByIds(List<Long> ids);

    /**
     * 修改菜品
     * @param dishDTO
     */
    void updateDish(DishDTO dishDTO);

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    DishVO selectById(Long id);

    /**
     * 修改菜品的状态
     * @param status
     * @param id
     */
    void updateStatus(Integer status, Long id);

    /**
     * 根据分类id查询菜品数据
     * @param categoryId
     * @return
     */
    List<Dish> selectByCategoryId(Integer categoryId);


    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    List<DishVO> listWithFlavor(Dish dish);
}
