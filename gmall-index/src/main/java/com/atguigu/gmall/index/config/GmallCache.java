package com.atguigu.gmall.index.config;

import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;

import java.lang.annotation.*;

/**
 * @author hehao
 * @create 2021-02-05 18:01
 */

@Target({ElementType.METHOD}) //注解注解在方法上
@Retention(RetentionPolicy.RUNTIME) //设置运行时注解
public @interface GmallCache {

    /**
     * 缓存的前缀
     * prefix + 方法的参数
     * @return
     */
    String prefix() default "";

    /**
     * 缓存的过期时间 单位为分钟
     * @return
     */
    int timeout() default 5;

    /**
     * 为了防止缓存雪崩，设置随机值范围
     * @return
     */
    int random() default 5;

    /**
     * 为了防止缓存击穿，需要分布式锁的前缀
     * @return
     */
    String preLock() default "";

}
