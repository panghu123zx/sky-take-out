package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import com.sky.result.PageResult;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
@Api(tags = "员工管理模块")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    @ApiOperation(value = "员工登入接口")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    @ApiOperation("员工退出接口")
    public Result<String> logout() {
        return Result.success();
    }


    /**
     * 新增
     * @Params employeeDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增员工的接口")
    public Result employInsert(@RequestBody EmployeeDTO employeeDTO){
        log.info("新增员工...");
        employeeService.employInsert(employeeDTO);

        return Result.success();
    }

    /**
     * 员工的分页查询
     * @param employeePageQueryDTO
     * @return
     */

    @GetMapping("/page")
    @ApiOperation("员工的分页查询")
    public Result<PageResult> employPageQuery(EmployeePageQueryDTO employeePageQueryDTO){
        log.info("员工的分页查询");
        PageResult page= employeeService.QueryPage(employeePageQueryDTO);
        return Result.success(page);
    }

    /**
     * 员工的启用和禁用
     * @param status
     * @param id
     * @return
     */

    @PostMapping("/status/{status}")
    @ApiOperation("员工的启用和禁用")
    public Result EnableAndDisableEmployee(@PathVariable Integer status,Long id){
        log.info("启用和禁用员工的账号...");
        employeeService.updateStatus(status,id);
        return Result.success();
    }

    /**
     * 根据id查询用户信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("查询员工信息")
    public Result<Employee> selectById(@PathVariable Long id){
        log.info("根据id查询用户信息");
        Employee employee= employeeService.selectById(id);
        return Result.success(employee);
    }


    /**
     * 修改员工信息
     * @param employeeDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改员工信息")
    public Result updateEmployeeById(@RequestBody EmployeeDTO employeeDTO){
        log.info("修改员工信息...");
        employeeService.updateEmployeeById(employeeDTO);
        return Result.success();
    }

    /**
     * 修改密码
     * @return
     */
    @ApiOperation("修改密码")
    @PutMapping("/editPassword")
    public Result editPassword(@RequestBody PasswordEditDTO passwordEditDTO){
        log.info("修改密码...");
        employeeService.editPassword(passwordEditDTO);
        return Result.success();
    }
}
