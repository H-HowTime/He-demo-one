package com.atguigu.gmall.index.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.config.GmallCache;
import com.atguigu.gmall.index.feign.GmallPmsFeignClient;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.index.utils.DistributedLock;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author hehao
 * @create 2021-02-02 23:18
 */
@Service
public class IndexServiceImpl implements IndexService {

    @Autowired
    private GmallPmsFeignClient gmallPmsFeignClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "index:cates";

    @Autowired
    private DistributedLock distributedLock;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public List<CategoryEntity> getCategorys() {

        return this.gmallPmsFeignClient.queryCategoryByPid(0l).getData();
    }

    @Override
    @GmallCache(prefix = KEY_PREFIX,timeout = 43200,random = 7500,preLock = "index:cates:lock")
    public List<CategoryEntity> getLv2CategoriesByPid(Long pid) {
        ResponseVo<List<CategoryEntity>> lv2CategoriesByPid = this.gmallPmsFeignClient.getLv2CategoriesByPid(pid);
        List<CategoryEntity> categories = lv2CategoriesByPid.getData();
        return categories;
    }
    public List<CategoryEntity> getLv2CategoriesByPid1(Long pid) {
        //先查询缓存，缓存中有则直接返回 -- key使用前缀(模块名+缓存对象的类型)+pid
        String key = KEY_PREFIX + pid;
        String json1 = this.redisTemplate.opsForValue().get(key);
        if (!StringUtils.isEmpty(json1)) {
            //将字符串反序列化为List<CategoryEntity>
            return JSON.parseArray(json1, CategoryEntity.class);
        }
        //为了防止缓存击穿，添加分布式锁
        RLock lock = redissonClient.getLock("index:cates:" + pid);
        lock.lock();
        List<CategoryEntity> categories;
        try {
            //在请求等待获取锁的过程中，其他请求可能已经获取到数据，并放入到缓存中，所以可以再次查询缓存
            String json2 = this.redisTemplate.opsForValue().get(key);
            if (!StringUtils.isEmpty(json2)) {
                //将字符串反序列化为List<CategoryEntity>
                return JSON.parseArray(json2, CategoryEntity.class);
            }
            //缓存中没有，则去数据库中查找，并将数据放入到缓存中
            ResponseVo<List<CategoryEntity>> lv2CategoriesByPid = this.gmallPmsFeignClient.getLv2CategoriesByPid(pid);
            categories = lv2CategoriesByPid.getData();
            if (CollectionUtils.isEmpty(categories)) {
                //为了防止缓存穿透，即使是null也放入缓存，后面可以使用布隆过滤器解决
                this.redisTemplate.opsForValue().set(key, JSON.toJSONString(categories), 5, TimeUnit.MINUTES);
            } else {
                //为了防止缓存雪崩，给缓存时间添加随机值
                this.redisTemplate.opsForValue().set(key, JSON.toJSONString(categories), 30 + RandomUtils.nextInt(0, 10), TimeUnit.DAYS);
            }
        } finally {
            //解锁
            lock.unlock();
        }
        return categories;
    }

    @Override
    public void testLock() {
        //加锁
        RLock lock = redissonClient.getLock("lock");
        lock.lock(10, TimeUnit.SECONDS); //10秒之后自动解锁
        try {
            //获取到锁，执行业务逻辑
            String number = this.redisTemplate.opsForValue().get("number");
            if (number == null) {
                return;
            }
            int num = Integer.parseInt(number);
            this.redisTemplate.opsForValue().set("number", String.valueOf(++num));
//            try {
//                TimeUnit.SECONDS.sleep(100);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
        } finally {
//            lock.unlock();
        }


    }

    @Override
    public void testReadLock() {
        RReadWriteLock rwLock = redissonClient.getReadWriteLock("rwLock");
        rwLock.readLock().lock(30, TimeUnit.SECONDS);
        System.out.println("这是读的业务逻辑");
//        rwLock.readLock().unlock();
    }

    @Override
    public void testWriteLock() {
        RReadWriteLock rwLock = redissonClient.getReadWriteLock("rwLock");
        rwLock.writeLock().lock(30, TimeUnit.SECONDS);
        System.out.println("这是写的业务逻辑");
//        rwLock.writeLock().unlock();
    }

    @Override
    public void testCdl() {
        RCountDownLatch cdlLock = redissonClient.getCountDownLatch("cdlLock");
        cdlLock.trySetCount(5);
        //业务逻辑
        System.out.println("班长关门了");
        try {
            cdlLock.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void testStu() {
        RCountDownLatch cdlLock = redissonClient.getCountDownLatch("cdlLock");

        cdlLock.countDown();

        System.out.println("学生出来了");
    }

    public void testLock2() {
        //为了防止误删，给锁添加唯一标识
        String uid = UUID.randomUUID().toString();
        //加锁
        Boolean lock = distributedLock.tryLock("lock", uid, 30);
        if (true) {

            //获取到锁，执行业务逻辑
            String number = this.redisTemplate.opsForValue().get("number");
            if (number == null) {
                return;
            }
            int num = Integer.parseInt(number);
            this.redisTemplate.opsForValue().set("number", String.valueOf(++num));

            try {
                TimeUnit.SECONDS.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //sbuTestLock(uid);

            distributedLock.unLock("lock", uid);
        }


    }

    public void sbuTestLock(String uuid) {
        distributedLock.tryLock("lock", uuid, 30);
        System.out.println("这是测试可重入");
        distributedLock.unLock("lock", uuid);
    }


    public void testLock1() {
        //为了防止误删，给锁添加唯一标识
        String uid = UUID.randomUUID().toString();
        //加锁
        Boolean lock = this.redisTemplate.opsForValue().setIfAbsent("lock", uid, 3, TimeUnit.SECONDS);
        if (!lock) {
            try {
                //获取锁失败，等一会再尝试获取锁
                Thread.sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            testLock();
        } else {
            //获取到锁，执行业务逻辑
            String number = this.redisTemplate.opsForValue().get("number");
            if (number == null) {
                return;
            }
            int num = Integer.parseInt(number);
            this.redisTemplate.opsForValue().set("number", String.valueOf(++num));

            //执行完业务逻辑，释放锁
            //为了防止误删 需要判断是否是自己的锁
            //使用lua脚本来实现判断和删除具有原子性
            //spring data-redis会自动的预加载 DefaultRedisScript对象要使用带有返回值的构造方法
            String script = "if(redis.call('get',KEYS[1]) == ARGV[1]) then return redis.call('del',KEYS[1]) else return 0 end";
            this.redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList("lock"), uid);
//            if (StringUtils.equals(uid, this.redisTemplate.opsForValue().get("lock"))) {
//
//                this.redisTemplate.delete("lock");
//            }
        }
    }

}
