package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    /**
     * 新增员工
     * @param employee
     * @return
     */
    @Insert("insert into employee (username,name,password,phone,sex,id_number,status,create_time,update_time,create_user,update_user)" +
            "values " +
            "(#{username},#{name},#{password},#{phone},#{sex},#{idNumber},#{status},#{createTime},#{updateTime},#{createUser},#{updateUser})")
    @AutoFill(OperationType.INSERT)
    void employInsert(Employee employee);


    /**
     *  分页查询
     * @param employeePageQueryDTO
     * @return
     */
    Page<Employee> QueryPage(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 修改员工的信息
     * @param employee
     */
    @AutoFill(OperationType.UPDATE)
    void update(Employee employee);

    /**
     * 编辑员工的回显
     * @param id
     */
    @Select("select * from employee where id=#{id}")
    Employee selectById(Long id);

}
