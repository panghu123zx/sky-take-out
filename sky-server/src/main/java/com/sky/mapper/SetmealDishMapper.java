package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    /**
     * 收集 和套餐 关联的 菜品 的套餐id
     * @param ids
     * @return
     */
    List<Long> selectSetMealBydishId(List<Long> ids);

    /**
     * 新菜品和套餐之间的关系
     * @param setmealDish
     */
    void insertSetmeal(SetmealDish setmealDish);

    /**
     * 根据套餐的id查询关系
     * @param id
     * @return
     */
    @Select("select * from setmeal_dish where setmeal_id=#{id}")
    List<SetmealDish> selectBySetmealId(Long id);

    /**
     * 删除和这个套餐的所有菜品
     * @param id
     */
    @Delete("delete from setmeal_dish where setmeal_id=#{id}")
    void deleteBySetmealId(Long id);
}
