package com.atguigu.gmall.cart.config;

import com.atguigu.gmall.cart.exception.CartUncaughtExceptionHandler;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;

import java.util.concurrent.Executor;

/**
 * @author hehao
 * @create 2021-02-24 0:13
 */
@Configuration
public class CartAsyncConfig implements AsyncConfigurer {

    @Autowired
    private CartUncaughtExceptionHandler cartUncaughtExceptionHandler;

    /**
     * 配置线程池，约束线程数
     *
     * @return
     */
    @Override
    public Executor getAsyncExecutor() {
        return null;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return cartUncaughtExceptionHandler;
    }
}
