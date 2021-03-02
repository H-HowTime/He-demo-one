package com.atguigu.gmall.search;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.feign.GmallPmsFeignClient;
import com.atguigu.gmall.search.feign.GmallWmsFeignClient;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchAttrValue;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
class GmallSearchApplicationTests {

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    private GoodsRepository goodsRepository;

    @Test
    void contextLoads() {
        //生成es索引库
        elasticsearchRestTemplate.createIndex(Goods.class);
        elasticsearchRestTemplate.putMapping(Goods.class);
    }

    @Autowired
    private GmallPmsFeignClient gmallPmsFeignClient;

    @Autowired
    private GmallWmsFeignClient gmallWmsFeignClient;

    @Test
    public void addData() {
        //从mysql数据库中获取数据批量添加到es索引库中
        Integer pageNum = 1;
        Integer pageSize = 100;
        do {
            //获取spu分页数据
            ResponseVo<List<SpuEntity>> spuByPage = this.gmallPmsFeignClient.querySpuByPage(new PageParamVo(pageNum, pageSize, null));
            List<SpuEntity> spuList = spuByPage.getData();
            if (CollectionUtils.isEmpty(spuList)) {
                break;
            }
            //查询spu下的所有sku
            spuList.forEach(spuEntity -> {
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
            });
            pageSize = spuList.size();
            pageNum++;
        } while (pageSize == 100);
    }
}
