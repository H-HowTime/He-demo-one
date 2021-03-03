package com.atguigu.gmall.index.utils;

import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author hehao
 * @create 2021-02-04 20:53
 */

@Component
@Slf4j
public class DistributedLock {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private Timer timer;

    //加锁
    public Boolean tryLock(String lockName, String uuid, Integer timeout) {
        String script = "if(redis.call('exists',KEYS[1]) == 0 or redis.call('hexists',KEYS[1],ARGV[1]) == 1) then " +
                "redis.call('hincrby',KEYS[1],ARGV[1],1)  " +
                "redis.call('expire',KEYS[1],ARGV[2]) " +
                "return 1 " +
                "else " +
                "return 0 " +
                "end";
        Boolean flag = this.stringRedisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList(lockName), uuid, timeout.toString());
        if (!flag) {
            //获取锁失败，睡一会在尝试获取锁
            try {
                Thread.sleep(50);
                this.tryLock(lockName, uuid, timeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //获取锁成功，自动续期
        this.renewExpire(lockName, uuid, timeout);
        return true;
    }

    //释放锁
    public void unLock(String lockName, String uuid) {
        //此处返回值类型不要使用Boolean，nil也会被解析成false
        //返回0, 则释放一次重入锁
        //返回1，则释放锁成功
        String script = "if(redis.call('hexists',KEYS[1],ARGV[1]) == 0) then " +
                "return nil " +
                "elseif(redis.call('hincrby',KEYS[1],ARGV[1],-1) == 0) then " +
                "return redis.call('del',KEYS[1]) " +
                "else " +
                "return 0 " +
                "end";
        //此处返回值类型不要使用Boolean，nil也会被解析成false
        Long execute = this.stringRedisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList(lockName), uuid);
        //返回null值，锁不存在，或者不是自己的锁
        if (execute == null) {
            log.error("锁不存在，或者不是自己的锁。锁的名称：{}，UUID:{}", lockName, uuid);
        }else if(execute == 1){
            //释放锁成功，取消定时任务
            timer.cancel();
        }
    }

    //自动续期
    public void renewExpire(String lockName, String uuid, Integer timeout) {
        String script = "if(redis.call('hexists',KEYS[1],ARGV[1]) == 1) then " +
                "return redis.call('expire',KEYS[1],ARGV[2]) " +
                "else " +
                "return 0 " +
                "end";
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                stringRedisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList(lockName), uuid, timeout.toString());
            }
        }, timeout * 1000 / 3, timeout * 1000 / 3);

    }

    //    public static void main(String[] args) {
//        System.out.println("开始时间：" +System.currentTimeMillis());
//        //Timer是jdk提供的一个定时器
//        new Timer().schedule(new TimerTask() {
//            @Override
//            public void run() {
//                System.out.println("这是jdk提供的定时器" + System.currentTimeMillis());
//            }
//        },5000,10000);
//    }
}
