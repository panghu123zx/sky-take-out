package com.sky.service;

import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.result.PageResult;

import java.util.List;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    /**
     * 新增员工
     * @param employeeDTO
     * @return
     */
    void employInsert(EmployeeDTO employeeDTO);

    /**
     *  员工的分页插叙
     * @param employeePageQueryDTO
     * @return
     */
    PageResult QueryPage(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 启用 或禁用 员工
     * @param status
     * @param id
     */
    void updateStatus(Integer status, Long id);

    /**
     * 修改员工信息
     * @param employeeDTO
     */
    void updateEmployeeById(EmployeeDTO employeeDTO);

    /**
     * 根据ID查询员工
     * @param id
     * @return
     */
    Employee selectById(Long id);

    /**
     * 修改密码
     * @param passwordEditDTO
     */
    void editPassword(PasswordEditDTO passwordEditDTO);
}
