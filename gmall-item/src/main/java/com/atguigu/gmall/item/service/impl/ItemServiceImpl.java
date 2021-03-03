package com.atguigu.gmall.item.service.impl;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.item.feign.GmallPmsFeignClient;
import com.atguigu.gmall.item.feign.GmallSmsFeignClient;
import com.atguigu.gmall.item.feign.GmallWmsFeignClient;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.item.vo.ItemVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.atguigu.gmall.pms.vo.SaleAttrVo;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author hehao
 * @create 2021-02-19 23:46
 */
@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private GmallPmsFeignClient gmallPmsFeignClient;

    @Autowired
    private GmallSmsFeignClient gmallSmsFeignClient;

    @Autowired
    private GmallWmsFeignClient gmallWmsFeignClient;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private TemplateEngine templateEngine;

    @Override
    public ItemVo queryItem(Long skuId) {
        ItemVo itemVo = new ItemVo();

        CompletableFuture<SkuEntity> skuFuture = CompletableFuture.supplyAsync(() -> {
            ResponseVo<SkuEntity> skuEntityResponseVo = gmallPmsFeignClient.querySkuById(skuId);
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if (skuEntity == null) {
                return null;
            }
            //设置sku信息
            itemVo.setSkuId(skuEntity.getId());
            itemVo.setTitle(skuEntity.getTitle());
            itemVo.setSubTitle(skuEntity.getSubtitle());
            itemVo.setPrice(skuEntity.getPrice());
            itemVo.setWeight(skuEntity.getWeight());
            itemVo.setDefaultImage(skuEntity.getDefaultImage());
//            System.out.println(skuEntity);
            return skuEntity;
        }, threadPoolExecutor);

        //设置spu信息
        CompletableFuture<Void> spuFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<SpuEntity> spuEntityResponseVo = gmallPmsFeignClient.querySpuBySkuId(skuId);
            SpuEntity spuEntity = spuEntityResponseVo.getData();
            if (spuEntity != null) {
                itemVo.setSpuId(spuEntity.getId());
                itemVo.setSpuName(spuEntity.getName());
            }
        }, threadPoolExecutor);

        //设置123级分类
        CompletableFuture<Void> categoryFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            if (skuEntity != null) {
                ResponseVo<List<CategoryEntity>> CategoriesResponseVo = gmallPmsFeignClient.query123CategoryByCid(skuEntity.getCategoryId());
                List<CategoryEntity> categoryEntities = CategoriesResponseVo.getData();
                itemVo.setCategories(categoryEntities);
            }
        }, threadPoolExecutor);

        //设置品牌信息
        CompletableFuture<Void> brandFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            if (skuEntity != null) {
                ResponseVo<BrandEntity> brandEntityResponseVo = gmallPmsFeignClient.queryBrandById(skuEntity.getBrandId());
                BrandEntity brandEntity = brandEntityResponseVo.getData();
                if (brandEntity != null) {
                    itemVo.setBrandId(brandEntity.getId());
                    itemVo.setBrandName(brandEntity.getName());
                }
            }
        }, threadPoolExecutor);

        //设置sku图片列表
        CompletableFuture<Void> skuImagesFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<List<SkuImagesEntity>> skuImagesResponseVo = gmallPmsFeignClient.querySkuImagesBySkuId(skuId);
            List<SkuImagesEntity> skuImages = skuImagesResponseVo.getData();
            itemVo.setSkuImages(skuImages);
        }, threadPoolExecutor);

        //设置库存信息
        CompletableFuture<Void> storeFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<List<WareSkuEntity>> wareSkuResponseVo = gmallWmsFeignClient.queryWareSkuBySkuId(skuId);
            List<WareSkuEntity> wareSkuEntities = wareSkuResponseVo.getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                wareSkuEntities.forEach(wareSkuEntity -> {
                    if (wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0) {
                        itemVo.setStore(true);
                    }
                });
            }
        }, threadPoolExecutor);

        //设置营销信息
        CompletableFuture<Void> itemSaleFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<List<ItemSaleVo>> promotionResponseVo = gmallSmsFeignClient.promotionBySkuId(skuId);
            List<ItemSaleVo> ItemSaleVos = promotionResponseVo.getData();
            itemVo.setSales(ItemSaleVos);
        }, threadPoolExecutor);

        //设置当前spu下所有sku的属性信息
        CompletableFuture<Void> saleAttrsFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            if (skuEntity != null) {
                ResponseVo<List<SaleAttrVo>> saleAttrsResponseVo = gmallPmsFeignClient.querySaleAttrsBySpuId(skuEntity.getSpuId());
                List<SaleAttrVo> saleAttrVos = saleAttrsResponseVo.getData();
                itemVo.setSaleAttrs(saleAttrVos);
            }
        }, threadPoolExecutor);

        //设置当前sku的参数信息 {3:'黑色',4:'8G',5:'64G'}
        CompletableFuture<Void> saleAttrFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<List<SkuAttrValueEntity>> skuAttrValueResponse = gmallPmsFeignClient.querySkuAttrValueBySkuId(skuId);
            List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrValueResponse.getData();
            if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {
                Map<Long, String> map = skuAttrValueEntities.stream().collect(Collectors.toMap(skuAttrValueEntity -> skuAttrValueEntity.getAttrId(), skuAttrValueEntity -> skuAttrValueEntity.getAttrValue()));
                itemVo.setSaleAttr(map);
            }
        }, threadPoolExecutor);

        //设置销售属性值和skuId的映射关系
        CompletableFuture<Void> mappingFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            if (skuEntity != null) {
                ResponseVo<String> stringResponseVo = gmallPmsFeignClient.querySkuAttrMappingSkuId(skuEntity.getSpuId());
                String skuJsons = stringResponseVo.getData();
                itemVo.setSkuJsons(skuJsons);
            }
        }, threadPoolExecutor);

        //设置商品海报
        CompletableFuture<Void> spuDescFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            if (skuEntity != null) {
                ResponseVo<SpuDescEntity> spuDescEntityResponseVo = gmallPmsFeignClient.querySpuDescById(skuEntity.getSpuId());
                SpuDescEntity spuDesc = spuDescEntityResponseVo.getData();
                if (spuDesc != null && StringUtils.isNotBlank(spuDesc.getDecript())) {
                    itemVo.setSpuImages(Arrays.asList(StringUtils.split(spuDesc.getDecript(), ",")));
                }
            }
        }, threadPoolExecutor);

        //设置分组规格参数列表
        CompletableFuture<Void> groupFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            if (skuEntity != null) {
                ResponseVo<List<ItemGroupVo>> ItemGroupVoResponseVo = gmallPmsFeignClient.queryAttrGroupValueByCidAndSpuIdAndSkuId(skuEntity.getCategoryId(), skuEntity.getSpuId(), skuId);
                List<ItemGroupVo> ItemGroupVos = ItemGroupVoResponseVo.getData();
                itemVo.setGroups(ItemGroupVos);
            }
        }, threadPoolExecutor);

        //当itemVo的信息设置完以后返回结果
        CompletableFuture.allOf(spuFuture, categoryFuture, brandFuture, groupFuture, spuDescFuture,
                mappingFuture, saleAttrFuture, saleAttrsFuture, itemSaleFuture, storeFuture, skuImagesFuture).join();
//        System.out.println(itemVo);
        return itemVo;
    }

    @Override
    public void buildHtml(ItemVo itemVo)  {
        //TODO 使用消息队列监控数据的修改
        //初始化上下文对象，通过对象给模板传递渲染所需要的的数据
        Context context = new Context();
        context.setVariable("itemVo", itemVo);
        //初始化文件流
        try (PrintWriter fileWriter = new PrintWriter("E:/deve_data/idea_data/workspace_idea/html/" + itemVo.getSkuId() + ".html");){
            //通过模板引擎生成一个静态页面 1-模板名称 2-上下文对象 3-文件流，生成的模板存到哪里
            templateEngine.process("item", context, fileWriter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
