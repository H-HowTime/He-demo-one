package com.atguigu.gmall.search.listener;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.feign.GmallPmsFeignClient;
import com.atguigu.gmall.search.feign.GmallWmsFeignClient;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchAttrValue;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import sun.awt.SunHints;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author hehao
 * @create 2021-02-02 22:10
 */
@Component
@Slf4j
public class GoodsListener {


    @Autowired
    private GmallPmsFeignClient gmallPmsFeignClient;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private GmallWmsFeignClient gmallWmsFeignClient;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "PMS_ITEM_QUEUE", durable = "true", ignoreDeclarationExceptions = "true"),
            exchange = @Exchange(value = "PMS_ITEM_EXCHANGE", durable = "true", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"item.insert"}
    ))
    public void goodsListener(Long spuId, Message message, Channel channel) throws IOException {
        if (spuId == null) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }

        //根据spuId获取spu信息
        ResponseVo<SpuEntity> spuEntityResponseVo = gmallPmsFeignClient.querySpuById(spuId);
        SpuEntity spuEntity = spuEntityResponseVo.getData();
        if (spuEntity == null) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }
        ResponseVo<List<SkuEntity>> skuListResponseVo = gmallPmsFeignClient.querySkuBySpuId(spuEntity.getId());
        List<SkuEntity> skuEntities = skuListResponseVo.getData();
        if (!CollectionUtils.isEmpty(skuEntities)) {
            List<Goods> goodsList = skuEntities.stream().map(skuEntity -> {
                Goods goods = new Goods();
                //设置sku信息
                goods.setSkuId(skuEntity.getId());
                goods.setPrice(skuEntity.getPrice().doubleValue());
                goods.setTitle(skuEntity.getTitle());
                goods.setSbuTitle(skuEntity.getSubtitle());
                goods.setDefaultImage(skuEntity.getDefaultImage());
                goods.setCreateTime(spuEntity.getCreateTime());
                //获取销量
                ResponseVo<List<WareSkuEntity>> wareSkuBySkuId = this.gmallWmsFeignClient.queryWareSkuBySkuId(skuEntity.getId());
                List<WareSkuEntity> wareSkuEntities = wareSkuBySkuId.getData();
                if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                    goods.setSales(wareSkuEntities.stream().map(WareSkuEntity::getSales).reduce((a, b) -> a + b).get());
                    goods.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
                }
                //品牌相关设置
                ResponseVo<BrandEntity> brandEntityResponseVo = this.gmallPmsFeignClient.queryBrandById(skuEntity.getBrandId());
                BrandEntity brandEntity = brandEntityResponseVo.getData();
                if (brandEntity != null) {
                    goods.setBrandId(brandEntity.getId());
                    goods.setBrandName(brandEntity.getName());
                    goods.setLogo(brandEntity.getLogo());
                }
                //分类相关设置
                ResponseVo<CategoryEntity> categoryEntityResponseVo = this.gmallPmsFeignClient.queryCategoryById(skuEntity.getCategoryId());
                CategoryEntity categoryEntity = categoryEntityResponseVo.getData();
                if (categoryEntity != null) {
                    goods.setCategoryId(categoryEntity.getId());
                    goods.setCategoryName(categoryEntity.getName());
                }
                //规格参数设置
                List<SearchAttrValue> searchAttrValues = new ArrayList<>();
                ResponseVo<List<SpuAttrValueEntity>> baseResVo = this.gmallPmsFeignClient.querySpuAttrValueBySpuIdAndCategoryId(skuEntity.getSpuId(), skuEntity.getCategoryId());
                List<SpuAttrValueEntity> spuAttrValueEntities = baseResVo.getData();
                if (!CollectionUtils.isEmpty(spuAttrValueEntities)) {
                    searchAttrValues.addAll(spuAttrValueEntities.stream().map(spuAttrValueEntity -> {
                        SearchAttrValue searchAttrValue = new SearchAttrValue();
                        BeanUtils.copyProperties(spuAttrValueEntity, searchAttrValue);
                        return searchAttrValue;
                    }).collect(Collectors.toList()));
                }
                ResponseVo<List<SkuAttrValueEntity>> salesResVo = this.gmallPmsFeignClient.querySkuAttrValueBySkuIdAndCategoryId(skuEntity.getId(), skuEntity.getCategoryId());
                List<SkuAttrValueEntity> skuAttrValueEntities = salesResVo.getData();
                if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {
                    searchAttrValues.addAll(skuAttrValueEntities.stream().map(skuAttrValueEntity -> {
                        SearchAttrValue searchAttrValue = new SearchAttrValue();
                        BeanUtils.copyProperties(skuAttrValueEntity, searchAttrValue);
                        return searchAttrValue;
                    }).collect(Collectors.toList()));
                }
                goods.setSearchAttrs(searchAttrValues);
                return goods;
            }).collect(Collectors.toList());
            goodsRepository.saveAll(goodsList);
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
