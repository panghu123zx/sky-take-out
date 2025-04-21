package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Component
@Aspect
@Slf4j
public class AutoFileAspect {
    //切点表达式
    @Pointcut("execution(* com.sky.mapper.*.*(..))  && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointcut() {
    }

    ;

    //使用前置通知，在update和insert方法之前 进行公共字段的生成
    @Before("autoFillPointcut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("开始公共字段的填充...");
        MethodSignature signature = (MethodSignature) joinPoint.getSignature(); //方法签名的对象
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);// 获取方法上面的注解对象
        OperationType operationType = autoFill.value(); //获取数据库的操作类型

        Object[] args = joinPoint.getArgs();  //获取到方法的参数

        //判断是否有参数
        if(args==null || args.length==0) return;

        Object entry = args[0];  //获取实体类对象,给实体类对象添加属性

        //获取准备要赋的值
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        //判断是那个方法，从而添加属性值
        if(operationType == operationType.INSERT){
            //添加4个属性值
            try {
                Method CreateTime = entry.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method CreateUser = entry.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method UpdateTime = entry.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method UpdateUser = entry.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                //通过反射进行赋值
                CreateTime.invoke(entry,now);
                CreateUser.invoke(entry,currentId);
                UpdateUser.invoke(entry,currentId);
                UpdateTime.invoke(entry,now);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        } else if (operationType==operationType.UPDATE) {
            //添加2个属性值
            try {
                Method UpdateTime = entry.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method UpdateUser = entry.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                //通过反射进行赋值
                UpdateUser.invoke(entry,currentId);
                UpdateTime.invoke(entry,now);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }


    }
}
