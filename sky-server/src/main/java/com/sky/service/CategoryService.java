package com.sky.service;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;

import java.util.List;

public interface CategoryService {
    /**
     * 分类的分页查询
     * @param categoryPageQueryDTO
     * @return
     */
     PageResult categoryQueryPage(CategoryPageQueryDTO categoryPageQueryDTO) ;

    /**
     * 新增分类
     */
    void categoryInsert(CategoryDTO categoryDTO);

    /**
     * 分类的启用和禁用
     * @param status
     * @param id
     */
    void updateStatus(Integer status, Long id);

    /**
     * 修改分类
     * @param categoryDTO
     */
    void updateCategory(CategoryDTO categoryDTO);

    /**
     * 删除分类
     * @param id
     */
    void deleteCategory(Long id);

    /**
     * 根据类型查询分类
     * @param type
     */
    List<Category> selectByType(Integer type);
}
