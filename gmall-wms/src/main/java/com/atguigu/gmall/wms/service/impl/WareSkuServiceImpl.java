package com.atguigu.gmall.wms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.exception.GmallException;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.wms.mapper.WareSkuMapper;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuMapper, WareSkuEntity> implements WareSkuService {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private WareSkuMapper wareSkuMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String LOCK_PREFIX = "store:lock:";
    private static final String LOCK_SUCCESS_PREFIX = "store:lock:success:";

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<WareSkuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageResultVo(page);
    }

    @Transactional
    @Override
    public List<SkuLockVo> checkAndLock(List<SkuLockVo> lockVos, String orderToken) {
        if (CollectionUtils.isEmpty(lockVos)) {
            throw new GmallException("没有你要购买的商品");
        }
        //遍历所有商品，进行验证库存和锁库存 要具有原子性 使用分布式锁框架redission加锁实现
        lockVos.forEach(lockVo -> {
            //操作库存信息，应该保证原子性，使用分布式锁框架redission加锁实现
            this.wareLock(lockVo);

        });
        //如果有一个商品验证库存失败，应该将验证成功的锁库存回滚，也就是解锁，避免重复锁库存
        if (lockVos.stream().anyMatch(lockVo -> !lockVo.getLock())) {
            //解锁
            lockVos.stream().filter(SkuLockVo::getLock).forEach(lockVo -> {
                Integer rows = this.wareSkuMapper.unLockStock(lockVo.getCount(), lockVo.getWareId());
            });
            //响应锁定状态
            return lockVos;
        }
        //如果所有商品都锁定成功的情况下，需要缓存锁定信息到redis中，已方便将来解锁库存 或者 减库存
        //以orderToken唯一标识，订单编号 以key 以lockVos锁定信息为值
        stringRedisTemplate.opsForValue().set(LOCK_SUCCESS_PREFIX + orderToken, JSON.toJSONString(lockVos));

        //所有商品锁定成功后，发送消息，定时解锁库存
        this.rabbitTemplate.convertAndSend("ORDER_EXCHANGE", "stock.unLock", orderToken);
        return null;
    }

    private void wareLock(SkuLockVo lockVo) {
        RLock fairLock = redissonClient.getFairLock(LOCK_PREFIX + lockVo.getSkuId());
        fairLock.lock();
        try {
            //验证库存： 查询 返回的满足要求的库存类表
            List<WareSkuEntity> wareSkuEntities = this.wareSkuMapper.selectStock(lockVo.getSkuId(), lockVo.getCount());
            //如果没有一个仓库满足条件，则验证库存失败
            if (CollectionUtils.isEmpty(wareSkuEntities)) {
                lockVo.setLock(false);
                return;
            }
            //应该根据大数据分析，距离近的仓库优先发货 -- 我们使用第一个满足的仓库
            WareSkuEntity wareSkuEntity = wareSkuEntities.get(0);
            //锁库存: 更新锁库存字段
            Integer rows = this.wareSkuMapper.updateLockStock(lockVo.getCount(), wareSkuEntity.getId());
            if (rows == 1) {
                //锁定库存成功
                lockVo.setLock(true);
                //保存锁库存成功的wareId，以方便后续解锁库存使用
                lockVo.setWareId(wareSkuEntity.getId());
            }
        } finally {
            fairLock.unlock();
        }
    }

}