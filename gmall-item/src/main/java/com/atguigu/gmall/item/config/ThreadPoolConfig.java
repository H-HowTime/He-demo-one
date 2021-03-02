package com.atguigu.gmall.item.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

/**
 * @author hehao
 * @create 2021-02-21 13:33
 */
@Configuration
public class ThreadPoolConfig {

    @Bean
    public ThreadPoolExecutor  threadPoolExecutor(
            @Value("${thread.pool.corePoolSize}") int corePoolSize,
            @Value("${thread.pool.maximumPoolSize}") int maximumPoolSize,
            @Value("${thread.pool.keepAliveTime}") long keepAliveTime,
            @Value("${thread.pool.workQueueSize}") int workQueueSize
            ) {
        return new ThreadPoolExecutor(corePoolSize,maximumPoolSize,keepAliveTime,TimeUnit.SECONDS,new ArrayBlockingQueue<>(workQueueSize));
    }
}
