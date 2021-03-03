package com.atguigu.gmall.cart.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @author hehao
 * @create 2021-02-24 0:10
 */
@Slf4j
@Component
public class CartUncaughtExceptionHandler implements AsyncUncaughtExceptionHandler {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String EXCEPTION_KEY = "cart:exception:info";

    @Override
    public void handleUncaughtException(Throwable throwable, Method method, Object... objects) {
        log.error("异常信息为：{}，方法为：{}，参数为：{}", throwable.getMessage(), method.getName(), objects);
        //将失败的购物车记录保存到redis中
        //获取userId
        String userId = objects[0].toString();
        BoundSetOperations<String, String> setOps = stringRedisTemplate.boundSetOps(EXCEPTION_KEY);
        setOps.add(userId);
    }
}
