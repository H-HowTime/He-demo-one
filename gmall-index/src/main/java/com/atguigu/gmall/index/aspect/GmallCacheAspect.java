package com.atguigu.gmall.index.aspect;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.index.config.GmallCache;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author hehao
 * @create 2021-02-05 19:55
 */
@Aspect //标识该类为一个切面类
@Component
public class GmallCacheAspect {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RBloomFilter rBloomFilter;

//    //execution声明一个切入点表达式 1-返回值 2-service包下类的所有方法..表示任何形参
//    @Before("execution(* com.atguigu.gmall.index.service.*.*(..) )")
//    public void before(JoinPoint joinPoint){
//        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
//        Object[] args = joinPoint.getArgs();
//        Class<?> aClass = joinPoint.getTarget().getClass();
//    }
//    @Around("execution(* com.atguigu.gmall.index.service.*.*(..) ))")
//    public void around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
//        proceedingJoinPoint.proceed(proceedingJoinPoint.getArgs());
//    }
//
//    @Pointcut("execution(* com.atguigu.gmall.index.service.*.*(..) ))")
//    public void pointcut(){}

    @Around("@annotation(com.atguigu.gmall.index.config.GmallCache)")
    public Object categoryAround(ProceedingJoinPoint point) throws Throwable {
        //获取方法签名
        MethodSignature signature = (MethodSignature) point.getSignature();
        //获取方法对象
        Method method = signature.getMethod();
        //获取方法返回值类型
        Class returnType = signature.getReturnType();
        //获取方法注解对象
        GmallCache gmallCache = method.getAnnotation(GmallCache.class);
        //组装缓存key
        String prefix = gmallCache.prefix();
        List<Object> args = Arrays.asList(point.getArgs());
        String key = prefix + args;

        //使用布隆过滤器，过滤请求，防止缓存穿透
        boolean contains = rBloomFilter.contains(key);
        if (!contains) {
            return null;
        }
        //1、查询缓存，如果命中则直接返回
        String json1 = stringRedisTemplate.opsForValue().get(key);
        if (!StringUtils.isBlank(json1)) {
            return JSON.parseObject(json1, returnType);
        }
        //2、为了防止缓存击穿，使用分布式锁
        RLock lock = redissonClient.getLock(gmallCache.preLock() + args);
        lock.lock();
        try {
            //3、因为在请求等待获取锁的时候，可能已经有请求获取到数据，并放入缓存中，再次查询缓存，存在则直接返回
            String json2 = stringRedisTemplate.opsForValue().get(key);
            if (!StringUtils.isBlank(json2)) {
                return JSON.parseObject(json2, returnType);
            }
            //4、执行目标方法，远程调用或者访问数据库，获取数据
            Object result = point.proceed(point.getArgs());
            if (result != null) {
                //为了防止缓存穿透，即使为null也放入缓存 使用布隆过滤器来过滤请求
//                stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(result), 3, TimeUnit.MINUTES);
                //为了防止缓存雪崩，给缓存设置随机过期时间范围
                stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(result), gmallCache.timeout() + new Random().nextInt(gmallCache.random()), TimeUnit.MINUTES);
            }
            return result;
        } finally {
            lock.unlock();
        }
    }
}
