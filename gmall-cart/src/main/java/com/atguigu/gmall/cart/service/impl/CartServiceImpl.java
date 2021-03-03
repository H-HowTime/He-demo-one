package com.atguigu.gmall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.entity.Cart;
import com.atguigu.gmall.cart.entity.UserInfo;
import com.atguigu.gmall.cart.feign.GmallPmsFeignClient;
import com.atguigu.gmall.cart.feign.GmallSmsFeignClient;
import com.atguigu.gmall.cart.feign.GmallWmsFeignClient;
import com.atguigu.gmall.cart.intercept.LoginInterceptHandler;
import com.atguigu.gmall.cart.mapper.CartMapper;
import com.atguigu.gmall.cart.service.AsyncService;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.exception.GmallException;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.concurrent.ListenableFuture;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * @author hehao
 * @create 2021-02-23 17:19
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private GmallPmsFeignClient gmallPmsFeignClient;

    @Autowired
    private GmallSmsFeignClient gmallSmsFeignClient;

    @Autowired
    private GmallWmsFeignClient gmallWmsFeignClient;

    @Autowired
    private AsyncService asyncService;


    private static final String KEY_PREFIX = "cart:info:";
    private static final String PRICE_PREFIX = "cart:price:";

    @Override
    public void saveCart(Cart cart) {
        if (cart == null || cart.getSkuId() == null) {
            throw new GmallException("没有该商品");
        }
        Long skuId = cart.getSkuId();
        System.out.println(skuId);
        //获取登录信息
        String key = getUserId();
        //获取当前用户的购物车信息
        BoundHashOperations<String, Object, Object> operations = stringRedisTemplate.boundHashOps(KEY_PREFIX + key);//获取用户对应的购物车商品
        Object cartJson = operations.get(String.valueOf(skuId));
        if (cartJson != null) {
            Cart cart2redis = JSON.parseObject(cartJson.toString(), Cart.class);
            //判断当前用户的购物车中是否包含该商品
            if (cart2redis != null) {
                //包含，则更新数量
                cart2redis.setCount(cart2redis.getCount().add(cart.getCount()));
                //更新redis
                operations.put(skuId.toString(), JSON.toJSONString(cart2redis));
                //异步更新mysql
                this.asyncService.updateCart(key, skuId, cart2redis);
            }
        } else {
            //不包含，则新增一条购物车记录
            cart.setUserId(key);
            //获取sku相关的信息
            ResponseVo<SkuEntity> skuEntityResponseVo = gmallPmsFeignClient.querySkuById(skuId);
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if (skuEntity != null) {
                cart.setTitle(skuEntity.getTitle());
                cart.setPrice(skuEntity.getPrice());
                cart.setDefaultImage(skuEntity.getDefaultImage());
            }
            //获取销售属性
            ResponseVo<List<SkuAttrValueEntity>> skuAttrValueResponseVo = gmallPmsFeignClient.querySkuAttrValueBySkuId(skuId);
            List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrValueResponseVo.getData();
            if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {
                cart.setSaleAttrs(JSON.toJSONString(skuAttrValueEntities));
            }
            //获取库存信息
            ResponseVo<List<WareSkuEntity>> WareSkuResponseVo = gmallWmsFeignClient.queryWareSkuBySkuId(skuId);
            List<WareSkuEntity> wareSkuEntities = WareSkuResponseVo.getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                cart.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
            }
            //获取优惠信息
            ResponseVo<List<ItemSaleVo>> promotionResponseVo = gmallSmsFeignClient.promotionBySkuId(skuId);
            List<ItemSaleVo> itemSaleVos = promotionResponseVo.getData();
            if (!CollectionUtils.isEmpty(itemSaleVos)) {
                cart.setSales(JSON.toJSONString(itemSaleVos));
            }
            //获取选中状态
            cart.setCheck(true);
            //保存购物车数据到redis中
            operations.put(skuId.toString(), JSON.toJSONString(cart));
            //异步保存购物车数据到mysql中
            this.asyncService.saveCart(cart.getUserId().toLowerCase(), cart);
            //在保存购物车记录时，添加价格缓存
            this.stringRedisTemplate.opsForValue().set(PRICE_PREFIX + skuId, skuEntity.getPrice().toString());
        }

    }


    private String getUserId() {
        UserInfo userInfo = LoginInterceptHandler.getUserInfo();
        Long userId = userInfo.getUserId();
        //获取redis中hash数据模型外部的key
        String key = userInfo.getUserKey();
        if (userId != null) {
            //userId不为空，为登录状态
            key = userId.toString();
        }
        return key;
    }

    @Override
    public Cart queryCartBySkuId(Long skuId) {
        //获取用户信息
        String userId = getUserId();
        //去Redis中获取购物车数据
        BoundHashOperations<String, Object, Object> operations = stringRedisTemplate.boundHashOps(KEY_PREFIX + userId);
        String cartJson = operations.get(skuId.toString()).toString();
        Cart cart = JSON.parseObject(cartJson, Cart.class);
        if (cart != null) {
            return cart;
        }
        throw new GmallException("该用户的购物车不包含该记录");
    }

    @Override
    @Async
    public void executor1() {
        System.out.println("executor1方法执行了");
        try {
            Thread.sleep(5000);
            int i = 12 / 0;
            System.out.println("executor1方法执行结束了");

        } catch (InterruptedException e) {
            e.printStackTrace();

        }

    }

    @Override
    @Async
    public void executor2() {
        System.out.println("executor2方法执行了");
        try {
            Thread.sleep(4000);
            System.out.println("executor2方法执行结束了");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Cart> showCart() {
        //1、获取用户状态信息 userKey
        UserInfo userInfo = LoginInterceptHandler.getUserInfo();
        //组装key
        String userKey = userInfo.getUserKey();
        String unLoginKey = KEY_PREFIX + userKey;
        //2、根据userKey查询未登录购物车记录
        //获取未登录购物车的内层map
        BoundHashOperations<String, Object, Object> unLoginHashOps = this.stringRedisTemplate.boundHashOps(unLoginKey);
        List<Object> unLoginCartsObj = unLoginHashOps.values();
        List<Cart> unLoginCarts = null;
        if (!CollectionUtils.isEmpty(unLoginCartsObj)) {
            unLoginCarts = unLoginCartsObj.stream().map(unLoginCartJson -> {
                Cart cart = JSON.parseObject(unLoginCartJson.toString(), Cart.class);
                //设置实时价格
                cart.setCurrentPrice(new BigDecimal(this.stringRedisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId())));
                return cart;
            }).collect(Collectors.toList());
        }
        //3、判断是否登录：userId是否为null
        Long userId = userInfo.getUserId();
        if (userId == null) {
            //4、如果没有登录，把根据userKey查询到未登录购物车记录返回给用户
            return unLoginCarts;
        }
        //5、如果存在未登录的购物车记录，则合并购物车记录
        String loginKey = KEY_PREFIX + userId;
        BoundHashOperations<String, Object, Object> loginHashOps = this.stringRedisTemplate.boundHashOps(loginKey);
        if (!CollectionUtils.isEmpty(unLoginCarts)) {
            for (Cart unLoginCart : unLoginCarts) { //为登录状态的购物车记录
                //判断登录购物车记录是否包含未登录购物车记录
                if (loginHashOps.hasKey(unLoginCart.getSkuId().toString())) {
                    BigDecimal unLoginCartCount = unLoginCart.getCount();
                    String loginCartJson = loginHashOps.get(unLoginCart.getSkuId().toString()).toString();
                    unLoginCart = JSON.parseObject(loginCartJson, Cart.class);
                    //包含：更新购物车条数
                    unLoginCart.setCount(unLoginCart.getCount().add(unLoginCartCount));
                    //更新redis
                    loginHashOps.put(unLoginCart.getSkuId().toString(), JSON.toJSONString(unLoginCart));
                    //异步更新mysql
                    //this.cartMapper.update(unLoginCart, new UpdateWrapper<Cart>().eq("user_id", userId.toString()).eq("sku_id", unLoginCart.getSkuId()));
                    this.asyncService.updateCart(userId.toString(), unLoginCart.getSkuId(), unLoginCart);
                } else {
                    //不包含：新增购物车记录
                    //添加redis
                    unLoginCart.setUserId(userId.toString()); //设置登录状态的userId
                    loginHashOps.put(unLoginCart.getSkuId().toString(), JSON.toJSONString(unLoginCart));
                    //异步添加mysql
//                    this.cartMapper.insert(unLoginCart);
                    this.asyncService.saveCart(userId.toString(), unLoginCart);
                }
            }
            // 6、删除未登录的购物车记录
            //删除redis中的未登录购物车记录
            this.stringRedisTemplate.delete(unLoginKey);
            //异步删除mysql中的数据
            this.asyncService.deleteCart(userKey);
        }
        //7、查询登录购物车记录返回给用户
        List<Object> loginCrtsObj = loginHashOps.values();
        if (CollectionUtils.isEmpty(loginCrtsObj)) {
            return null;
        }
        return loginCrtsObj.stream().map(unLoginCartJson -> {
            Cart cart = JSON.parseObject(unLoginCartJson.toString(), Cart.class);
            //设置实时价格
            cart.setCurrentPrice(new BigDecimal(this.stringRedisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId())));
            return cart;
        }).collect(Collectors.toList());
    }

    @Override
    public void updateNum(Cart cart) {
        //获取用户登录信息
        String userId = this.getUserId();
        String key = KEY_PREFIX + userId;
        BoundHashOperations<String, Object, Object> hashOps = this.stringRedisTemplate.boundHashOps(key);
        if (hashOps.hasKey(cart.getSkuId().toString())) {
            String cartJson = hashOps.get(cart.getSkuId().toString()).toString();
            if (StringUtils.isNotBlank(cartJson)) {
                BigDecimal count = cart.getCount();
                cart = JSON.parseObject(cartJson, Cart.class);
                cart.setCount(count);
                //更新redis
                hashOps.put(cart.getSkuId().toString(), JSON.toJSONString(cart));
                //异步更新mysql
                this.asyncService.updateCart(userId, cart.getSkuId(), cart);
            }
        }
    }

    @Override
    public void deleteCart(Long skuId) {
        //获取用户登录信息
        String userId = this.getUserId();
        String key = KEY_PREFIX + userId;
        BoundHashOperations<String, Object, Object> hashOps = this.stringRedisTemplate.boundHashOps(key);
        if (hashOps.hasKey(skuId.toString())) {
            //删除redis中的购物车记录
            hashOps.delete(skuId.toString());
            //异步删除mysql中的记录
            this.asyncService.deleteCartBySkuId(userId, skuId);
        }
    }

    @Override
    public List<Cart> checkSelCart(Long userId) {
        //查询redis中的用户购物车记录
        BoundHashOperations<String, Object, Object> hashOps = this.stringRedisTemplate.boundHashOps(KEY_PREFIX + userId);
        List<Object> cartJsons = hashOps.values();
        if (CollectionUtils.isEmpty(cartJsons)) {
            return null;
        }
        return cartJsons.stream().map(cartJson ->
                JSON.parseObject(cartJson.toString(), Cart.class)
        ).filter(cart -> cart.getCheck()).collect(Collectors.toList());
    }
}
