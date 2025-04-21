package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.DishFlavor;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {

    /**
     * 插入菜品的口味信息
     * @param flavors
     */
    void insert(List<DishFlavor> flavors);

    /**
     * 更具才菜品id删除 口味数据
     * @param dishId
     */
    @Delete("delete from dish_flavor where dish_id=#{dishId}")
    void deleteByDishId(Long dishId);

    /**
     * 菜品中口味的回显
     * @param id
     */
    @Select("select * from dish_flavor where dish_id=#{id}")
    List<DishFlavor> selectFlavor(Long id);

}
