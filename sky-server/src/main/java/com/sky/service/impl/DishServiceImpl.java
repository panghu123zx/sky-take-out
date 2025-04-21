package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 新增菜品
     * @param dishDTO
     */
    @Override
    public void dishInsert(DishDTO dishDTO) {
        //因为 传递的 DTO中还含有 dishFlavor中的数据
        Dish dish=new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.insert(dish);
        Long dishId = dish.getId(); //得到dish的id传递给 dish_flavor

        //返回的是一个List集合，有多条数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors!=null && flavors.size()>0){
            flavors.forEach(dishFlavor -> dishFlavor.setDishId(dishId));
            dishFlavorMapper.insert(flavors);
        }


    }

    /**
     * 菜品的分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult selectPage(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> page= dishMapper.selectPage(dishPageQueryDTO);
        long total = page.getTotal();
        List<DishVO> result = page.getResult();
        return new PageResult(total,result);
    }

    /**
     * 批量删除菜品管理
     * @param ids
     */
    @Override
    @Transactional
    public void deleteByIds(List<Long> ids) {
        //判断当前菜品是停售还是起售状态，起售不可以删除
        Long count= dishMapper.queryStatus(ids);  //收集起售的数量
        if(count>0){
            throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
        }
        //判断是否有关联的菜品，有就不可以删除
        List<Long> total= setmealDishMapper.selectSetMealBydishId(ids);
        if(total!=null && total.size()>0){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        //删除掉菜品对应的口味数据
        for (Long id : ids) {
            dishMapper.deleteById(id);
            //删除口味数据
            dishFlavorMapper.deleteByDishId(id);
        }
    }

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @Override
    public DishVO selectById(Long id) {
        //页面的回显
        DishVO dishVO= dishMapper.selectDish(id);  //菜品的回显
        List<DishFlavor> flavors= dishFlavorMapper.selectFlavor(id);  //口味的回显
        dishVO.setFlavors(flavors);
        return dishVO;
    }

    /**
     * 修改菜品
     * @param dishDTO
     */
    @Override
    public void updateDish(DishDTO dishDTO) {
        Dish dish=new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.updateDish(dish);  //修改菜品的信息
        //修改口味信息时，因为比较复杂，所以需要把全部的删除掉，然后再重新插入
        dishFlavorMapper.deleteByDishId(dishDTO.getId());  //删除掉所有的口味数据

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors!=null && flavors.size()>0){
            for (DishFlavor flavor : flavors) {
                flavor.setDishId(dishDTO.getId());
            }
        }
        dishFlavorMapper.insert(flavors);  //插入口味数据
    }

    /**
     * 修改菜品的状态
     * @param status
     * @param id
     */
    @Override
    public void updateStatus(Integer status, Long id) {
        Dish dish=new Dish();
        dish.setId(id);
        dish.setStatus(status);
        dishMapper.updateDish(dish);
    }

    /**
     * 根据分类id查询菜品数据
     * @param categoryId
     * @return
     */
    @Override
    public List<Dish> selectByCategoryId(Integer categoryId) {
        List<Dish> dishes=dishMapper.selectByCategoryId(categoryId);
        return dishes;
    }

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.selectFlavor(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }
}
