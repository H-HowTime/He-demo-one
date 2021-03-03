package com.atguigu.gmall.oms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.exception.GmallException;
import com.atguigu.gmall.oms.entity.OrderItemEntity;
import com.atguigu.gmall.oms.feign.GmallPmsFeignClient;
import com.atguigu.gmall.oms.feign.GmallSmsFeignClient;
import com.atguigu.gmall.oms.feign.GmallUmsFeignClient;
import com.atguigu.gmall.oms.feign.GmallWmsFeignClient;
import com.atguigu.gmall.oms.mapper.OrderItemMapper;
import com.atguigu.gmall.oms.vo.OrderItemVo;
import com.atguigu.gmall.oms.vo.OrderSubmitVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.ums.entity.UserAddressEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.oms.mapper.OrderMapper;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderMapper, OrderEntity> implements OrderService {

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private GmallPmsFeignClient gmallPmsFeignClient;

    @Autowired
    private GmallWmsFeignClient gmallWmsFeignClient;

    @Autowired
    private GmallSmsFeignClient gmallSmsFeignClient;

    @Autowired
    private GmallUmsFeignClient gmallUmsFeignClient;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<OrderEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<OrderEntity>()
        );

        return new PageResultVo(page);
    }

    @Transactional
    @Override
    public void downOrder(OrderSubmitVo orderSubmitVo, Long userId) {
        List<OrderItemVo> items = orderSubmitVo.getItems();
        if (CollectionUtils.isEmpty(items)) {
            throw new GmallException("你没有购买的订单商品");
        }
        //新增订单表
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setUserId(userId);
        orderEntity.setOrderSn(orderSubmitVo.getOrderToken());
        orderEntity.setCreateTime(new Date());
        orderEntity.setTotalAmount(orderSubmitVo.getTotalPrice());
        //TODO 总金额 + 运费 - 满减 - 打折 - 积分
        orderEntity.setPayAmount(orderSubmitVo.getTotalPrice());
        orderEntity.setPayType(orderSubmitVo.getPayType());
        orderEntity.setSourceType(0);
        orderEntity.setStatus(0);
        orderEntity.setDeliveryCompany(orderSubmitVo.getDeliveryCompany());
        //TODO  遍历所有商品，查询每个商品赠送的积分信息。累加
        orderEntity.setIntegration(1000);
        orderEntity.setGrowth(2000);
        UserAddressEntity address = orderSubmitVo.getAddress();
        if (address != null) {
            orderEntity.setReceiverAddress(address.getAddress());
            orderEntity.setReceiverRegion(address.getRegion());
            orderEntity.setReceiverCity(address.getCity());
            orderEntity.setReceiverName(address.getName());
            orderEntity.setReceiverPhone(address.getPhone());
            orderEntity.setReceiverPostCode(address.getPostCode());
            orderEntity.setReceiverProvince(address.getProvince());
        }
        orderEntity.setDeleteStatus(0);
        orderEntity.setUseIntegration(orderSubmitVo.getBounds());
        this.baseMapper.insert(orderEntity);
        Long orderEntityId = orderEntity.getId();
        //新增订单详情
        items.forEach(itemVo -> {
            OrderItemEntity orderItemEntity = new OrderItemEntity();
            orderItemEntity.setSkuQuantity(itemVo.getCount().intValue());
            orderItemEntity.setOrderId(orderEntityId);
            orderItemEntity.setOrderSn(orderSubmitVo.getOrderToken());
            //根据skuid查询sku相关的信息
            ResponseVo<SkuEntity> skuEntityResponseVo = gmallPmsFeignClient.querySkuById(itemVo.getSkuId());
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if (skuEntity != null) {
                orderItemEntity.setSkuId(skuEntity.getId());
                orderItemEntity.setSkuName(skuEntity.getName());
                orderItemEntity.setSkuPrice(skuEntity.getPrice());
                orderItemEntity.setSkuPic(skuEntity.getDefaultImage());
                orderItemEntity.setCategoryId(skuEntity.getCategoryId());
            }
            //sku的销售属性、
            ResponseVo<List<SkuAttrValueEntity>> skuAttrResponseVo = gmallPmsFeignClient.querySkuAttrValueBySkuId(itemVo.getSkuId());
            List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrResponseVo.getData();
            if (skuAttrValueEntities != null) {
                orderItemEntity.setSkuAttrsVals(JSON.toJSONString(skuAttrValueEntities));
            }

            //查询品牌
            ResponseVo<BrandEntity> brandEntityResponseVo = gmallPmsFeignClient.queryBrandById(skuEntity.getBrandId());
            BrandEntity brandEntity = brandEntityResponseVo.getData();
            if (brandEntity != null) {
                orderItemEntity.setSpuBrand(brandEntity.getName());
            }
            //查询spu信息
            ResponseVo<SpuEntity> spuEntityResponseVo = gmallPmsFeignClient.querySpuBySkuId(skuEntity.getId());
            SpuEntity spuEntity = spuEntityResponseVo.getData();
            if (spuEntity != null) {
                orderItemEntity.setSpuId(spuEntity.getId());
                orderItemEntity.setSpuName(spuEntity.getName());
            }
            //spu描述信息
            ResponseVo<SpuDescEntity> spuDescEntityResponseVo = gmallPmsFeignClient.querySpuDescById(spuEntity.getId());
            SpuDescEntity spuDescEntity = spuDescEntityResponseVo.getData();
            if (spuDescEntity != null) {
                orderItemEntity.setSpuPic(spuDescEntity.getDecript());
            }
            //TODO 查询商品赠送信息
            this.orderItemMapper.insert(orderItemEntity);
        });
    }

    @Override
    public OrderEntity queryOrderByOrderToken(String orderToken) {
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq("order_sn", orderToken);
        return this.baseMapper.selectOne(wrapper);
    }

}
