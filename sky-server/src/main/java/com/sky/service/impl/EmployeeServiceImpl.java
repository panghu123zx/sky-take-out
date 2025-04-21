package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.autoconfigure.PageHelperProperties;
import com.sky.annotation.AutoFill;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // 对前端传递过来的数据进行md5 加密处理
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 新增员工
     *
     * @param employeeDTO
     * @return
     */
    @Override
    public void employInsert(EmployeeDTO employeeDTO) {
        Employee employee=new Employee();
        //属性拷贝
        BeanUtils.copyProperties(employeeDTO,employee);

        //设置状态,密码,创建时间和更新时间
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        employee.setStatus(StatusConstant.ENABLE);

        employeeMapper.employInsert(employee);

    }


    /**
     *  员工的分页插叙
     * @param employeePageQueryDTO
     * @return
     */
    @Override
    public PageResult QueryPage(EmployeePageQueryDTO employeePageQueryDTO) {
        PageHelper.startPage(employeePageQueryDTO.getPage(),employeePageQueryDTO.getPageSize());
        Page<Employee> page= employeeMapper.QueryPage(employeePageQueryDTO);

        long total = page.getTotal();
        List<Employee> result = page.getResult();
        return new PageResult(total,result);
    }

    /**
     * 启用 或禁用 员工
     * @param status
     * @param id
     */
    @Override
    public void updateStatus(Integer status, Long id) {
        //为了代码的通用性，设置到对象里面
        Employee employee = Employee.builder()
                .status(status)
                .id(id)
                .build();
        employeeMapper.update(employee);
    }

    /**
     * 修改员工信息
     * @param employeeDTO
     */
    @Override
    public void updateEmployeeById(EmployeeDTO employeeDTO) {
        Employee employee=new Employee();
        BeanUtils.copyProperties(employeeDTO,employee);
        employeeMapper.update(employee);
    }

    /**
     * 根据id查询员工
     * @param id
     * @return
     */
    @Override
    public Employee selectById(Long id) {
        Employee employee = employeeMapper.selectById(id);
        employee.setPassword("****");
        return employee;
    }

    /**
     * 修改密码
     * @param passwordEditDTO
     */
    @Override
    @Transactional
    public void editPassword(PasswordEditDTO passwordEditDTO) {
        Long id = passwordEditDTO.getEmpId();
        Employee employee = employeeMapper.selectById(id);
        String OldPassword=DigestUtils.md5DigestAsHex(passwordEditDTO.getOldPassword().getBytes());
        if(!OldPassword.equals(employee.getPassword())){
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }
        Employee employee1 = Employee.builder().id(passwordEditDTO.getEmpId()).password(DigestUtils.md5DigestAsHex(passwordEditDTO.getNewPassword().getBytes())).build();
        employeeMapper.update(employee1);

    }
}
