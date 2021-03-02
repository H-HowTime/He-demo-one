package com.atguigu.gmall.order.servie.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.entity.Cart;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.exception.GmallException;
import com.atguigu.gmall.oms.vo.OrderItemVo;
import com.atguigu.gmall.oms.vo.OrderSubmitVo;
import com.atguigu.gmall.order.feign.*;
import com.atguigu.gmall.order.intercept.LoginInterceptHandler;
import com.atguigu.gmall.order.pojo.UserInfo;
import com.atguigu.gmall.order.servie.OrderService;
import com.atguigu.gmall.order.vo.ConfirmVo;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.ums.entity.UserAddressEntity;
import com.atguigu.gmall.ums.entity.UserEntity;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author hehao
 * @create 2021-02-26 23:40
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private GmallCartFeignClient gmallCartFeignClient;

    @Autowired
    private GmallPmsFeignClient gmallPmsFeignClient;

    @Autowired
    private GmallSmsFeignClient gmallSmsFeignClient;

    @Autowired
    private GmallUmsFeignClient gmallUmsFeignClient;

    @Autowired
    private GmallWmsFeignClient gmallWmsFeignClient;

    @Autowired
    private GmallOmsFeignClient gmallOmsFeignClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    private static final String TOKEN_KEY_PREFIX = "order:token:";

    @Override
    public ConfirmVo orderConfirm() {
        ConfirmVo confirmVo = new ConfirmVo();
        //获取用户userId
        UserInfo userInfo = LoginInterceptHandler.getUserInfo();
        Long userId = userInfo.getUserId();
        CompletableFuture<List<Cart>> cartsFuture = CompletableFuture.supplyAsync(() -> {
            ResponseVo<List<Cart>> cartResponseVo = this.gmallCartFeignClient.checkSelCart(userId);
            List<Cart> carts = cartResponseVo.getData();
            if (CollectionUtils.isEmpty(carts)) {
                throw new GmallException("没有选中的的购物车记录");
            }
            return carts;
        }, threadPoolExecutor);
        //设置商品清单列表
        CompletableFuture<Void> orderItemsFuture = cartsFuture.thenAcceptAsync(carts -> {
            List<OrderItemVo> orderItemVos = carts.stream().map(cart -> {
                OrderItemVo orderItemVo = new OrderItemVo();
                //获取sku信息
                CompletableFuture<Void> skuFuture = CompletableFuture.runAsync(() -> {
                    ResponseVo<SkuEntity> skuEntityResponseVo = this.gmallPmsFeignClient.querySkuById(cart.getSkuId());
                    SkuEntity skuEntity = skuEntityResponseVo.getData();
                    BeanUtils.copyProperties(skuEntity, orderItemVo);
                }, threadPoolExecutor);
                //获取库存信息
                CompletableFuture<Void> wareFuture = CompletableFuture.runAsync(() -> {
                    ResponseVo<List<WareSkuEntity>> wareResponseVo = this.gmallWmsFeignClient.queryWareSkuBySkuId(cart.getSkuId());
                    List<WareSkuEntity> wareSkuEntities = wareResponseVo.getData();
                    if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                        if (wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0)) {
                            orderItemVo.setStore(true);
                        }
                    }
                }, threadPoolExecutor);
                //获取销售属性
                CompletableFuture<Void> skuAttrFuture = CompletableFuture.runAsync(() -> {
                    ResponseVo<List<SkuAttrValueEntity>> skuAttrResponseVo = this.gmallPmsFeignClient.querySkuAttrValueBySkuId(cart.getSkuId());
                    List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrResponseVo.getData();
                    orderItemVo.setSaleAttrs(skuAttrValueEntities);
                }, threadPoolExecutor);
                //获取优惠信息
                CompletableFuture<Void> saleFuture = CompletableFuture.runAsync(() -> {
                    ResponseVo<List<ItemSaleVo>> salesResponseVo = this.gmallSmsFeignClient.promotionBySkuId(cart.getSkuId());
                    List<ItemSaleVo> itemSaleVos = salesResponseVo.getData();
                    orderItemVo.setSales(itemSaleVos);
                }, threadPoolExecutor);
                CompletableFuture.allOf(saleFuture, wareFuture, skuAttrFuture, skuFuture).join();
                //只取购车车中的skuId和count数据
                orderItemVo.setSkuId(cart.getSkuId());
                orderItemVo.setCount(cart.getCount());
                return orderItemVo;
            }).collect(Collectors.toList());
            confirmVo.setOrderItems(orderItemVos);
        }, threadPoolExecutor);
        //设置用户地址列表
        CompletableFuture<Void> userAddressFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<List<UserAddressEntity>> userResponseVo = this.gmallUmsFeignClient.queryUserAddressByUId(userId);
            List<UserAddressEntity> addressEntities = userResponseVo.getData();
            if (!CollectionUtils.isEmpty(addressEntities)) {
                confirmVo.setAddress(addressEntities);
            }
        }, threadPoolExecutor);
        //设置积分信息
        CompletableFuture<Void> userFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<UserEntity> userEntityResponseVo = this.gmallUmsFeignClient.queryUserById(userId);
            UserEntity userEntity = userEntityResponseVo.getData();
            if (userEntity != null) {
                confirmVo.setBounds(userEntity.getIntegration());
            }
        }, threadPoolExecutor);
        //订单详情页唯一标识 防止订单反复提交，避免接口幂等性问题
        CompletableFuture<Void> orderTokenFuture = CompletableFuture.runAsync(() -> {
            String timeId = IdWorker.getTimeId(); //IdWorker是mybatis对雪花算法的封装
            //保存到redis中一份
            this.stringRedisTemplate.opsForValue().set(TOKEN_KEY_PREFIX + timeId, timeId, 24, TimeUnit.HOURS);
            confirmVo.setOrderToken(timeId);
        }, threadPoolExecutor);
        CompletableFuture.allOf(orderItemsFuture, userAddressFuture, userFuture, orderTokenFuture).join();
        System.out.println(confirmVo);
        return confirmVo;
    }

    @Override
    public void orderSubmit(OrderSubmitVo orderSubmitVo) {
        //1、防重 redis
        String orderToken = orderSubmitVo.getOrderToken();
        if (StringUtils.isBlank(orderToken)) {
            throw new GmallException("非法提交");
        }
        //使用lua脚本保证查询和删除的原子性
        String script = "if(redis.call('get',KEYS[1]) == ARGV[1]) then return redis.call('del',KEYS[1]) else return 0 end";
        if (!stringRedisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList(TOKEN_KEY_PREFIX + orderToken), orderToken)) {
            throw new GmallException("请不要重复提交");
        }
        //2、验证总价：遍历送货清单，获取数据库的实时价格*数量 最后再累加 mysql
        BigDecimal totalPrice = orderSubmitVo.getTotalPrice();
        List<OrderItemVo> itemVos = orderSubmitVo.getItems();
        if (CollectionUtils.isEmpty(itemVos)) {
            throw new GmallException("请选中你要购买的商品");
        }
        BigDecimal currentTotalPrice = itemVos.stream().map(itemVo -> {
            ResponseVo<SkuEntity> skuEntityResponseVo = this.gmallPmsFeignClient.querySkuById(itemVo.getSkuId());
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if (skuEntity != null) {
                return skuEntity.getPrice().multiply(itemVo.getCount());
            }
            return new BigDecimal(0);
        }).reduce(BigDecimal::add).get();
        if (currentTotalPrice.compareTo(totalPrice) != 0) {
            throw new GmallException("商品价格有所变动，请刷新重试");
        }
        //3、验证并锁定库存 远程调用wms模块
        List<SkuLockVo> lockVos = itemVos.stream().map(itemVo -> {
            SkuLockVo skuLockVo = new SkuLockVo();
            skuLockVo.setSkuId(itemVo.getSkuId());
            skuLockVo.setCount(itemVo.getCount().intValue());
            return skuLockVo;
        }).collect(Collectors.toList());
        ResponseVo<List<SkuLockVo>> skuLockResponseVo = this.gmallWmsFeignClient.checkAndLock(lockVos, orderToken);
        List<SkuLockVo> skuLockVos = skuLockResponseVo.getData();
        if (!CollectionUtils.isEmpty(skuLockVos)) {
            //锁库存失败
            throw new GmallException(JSON.toJSONString(skuLockVos));
        }
        //4、下单 远程调用oms模块操作订单相关的表
        UserInfo userInfo = LoginInterceptHandler.getUserInfo();
        Long userId = userInfo.getUserId();
        try {
            this.gmallOmsFeignClient.downOrder(orderSubmitVo, userId);
            //订单正常的创建成功的情况下，发送消息定时关单 使用rabbitmq的延时队列 + 死信队列完成定时任务
            this.rabbitTemplate.convertAndSend("ORDER_EXCHANGE", "close.order", orderToken);
        } catch (Exception e) {
            e.printStackTrace();
            //订单创建失败有两种情况 使用rabbitmq异步处理
            //1-创建订单时出现错误 此时要去解锁库存
            //2-订单创建成功，响应失败 此时要去解锁库存，并将订单标记为无效订单
            this.rabbitTemplate.convertAndSend("ORDER_EXCHANGE", "order.disable", orderToken);
            throw new GmallException("创建订单服务器错误");
        }
        //5、下单成功删除购物车对应的记录 -- 使用rabbitmq异步删除对应的购物出记录 消息userId和对应选中购物车的skuId
        Map<String, Object> selectedCartsInfo = new HashMap<>();
        selectedCartsInfo.put("userId", userId);
        //获取对应的skuId集合
        List<Long> skuIds = itemVos.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
        selectedCartsInfo.put("skuIds", JSON.toJSONString(skuIds));
        this.rabbitTemplate.convertAndSend("ORDER_EXCHANGE", "cart.delete", selectedCartsInfo);
    }
}
