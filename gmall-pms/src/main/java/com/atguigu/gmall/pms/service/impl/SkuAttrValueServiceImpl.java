package com.atguigu.gmall.pms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pms.vo.SaleAttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;


@Service("skuAttrValueService")
public class SkuAttrValueServiceImpl extends ServiceImpl<SkuAttrValueMapper, SkuAttrValueEntity> implements SkuAttrValueService {

    @Autowired
    private AttrMapper attrMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuAttrValueEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuAttrValueEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<SkuAttrValueEntity> querySkuAttrValueBySkuIdAndCategoryId(Long skuId, Long categoryId) {
        //根据spuId和categoryId 获取attr信息
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("category_id", categoryId);
        wrapper.eq("search_type", 1);
        List<AttrEntity> attrEntities = attrMapper.selectList(wrapper);
        if (CollectionUtils.isEmpty(attrEntities)) {
            return null;
        }
        List<Long> ids = attrEntities.stream().map(AttrEntity::getId).collect(Collectors.toList());
        return this.list(new QueryWrapper<SkuAttrValueEntity>().eq("sku_id", skuId).in("attr_id", ids));
    }

    @Override
    public List<SaleAttrVo> querySaleAttrsBySpuId(Long spuId) {
        //获取spu下的所有sku信息以及对应的规格参数值
        //[{attrId:3,attrName:'机身颜色',attrValue:['黑色','白色']},
        // {attrId:4,attrName:'运行内存',attrValue:'['8G','16G']},
        // {attrId:5,attrName:'机身存储',attrValue:['64G','128G']},]
        //获取所有的sku
        List<SkuEntity> skuEntities = this.skuMapper.selectList(new QueryWrapper<SkuEntity>().eq("spu_id", spuId));
        if (CollectionUtils.isEmpty(skuEntities)) {
            return null;
        }
        //收集sku的id集合
        List<Long> skuIds = skuEntities.stream().map(SkuEntity::getId).collect(Collectors.toList());
        //根据sku的id集合查询对应的规格参数信息
        List<SkuAttrValueEntity> skuAttrValueEntities = this.baseMapper.selectList(new QueryWrapper<SkuAttrValueEntity>().in("sku_id", skuIds).orderByAsc("attr_id"));
        if (CollectionUtils.isEmpty(skuAttrValueEntities)) {
            return null;
        }
        //转化为SaleAttrVo对象的集合
        //map中以attrId -- key  对应的所用行数据 -- value
        Map<Long, List<SkuAttrValueEntity>> map = skuAttrValueEntities.stream().collect(Collectors.groupingBy(t -> t.getAttrId()));
        //将map中的每一个元素转化为SaleAttrVo对象
        List<SaleAttrVo> saleAttrVoList = new ArrayList<>();
        map.forEach((attrId, skuAttrValueEntityList) -> {
            SaleAttrVo saleAttrVo = new SaleAttrVo();
            saleAttrVo.setAttrId(attrId);
            saleAttrVo.setAttrName(skuAttrValueEntityList.get(0).getAttrName());
            //将attrValue收集为一个set集合
            Set<String> attrValueSet = skuAttrValueEntityList.stream().map(SkuAttrValueEntity::getAttrValue).collect(Collectors.toSet());
            saleAttrVo.setAttrValues(attrValueSet);
            saleAttrVoList.add(saleAttrVo);
        });
        return saleAttrVoList;
    }

    @Override
    public List<SkuAttrValueEntity> querySkuAttrValueBySkuId(Long skuId) {
        List<SkuAttrValueEntity> skuAttrValueEntities = this.baseMapper.selectList(new QueryWrapper<SkuAttrValueEntity>().eq("sku_id", skuId));

        return skuAttrValueEntities;
    }

    @Override
    public String querySkuAttrMappingSkuId(Long spuId) {
        //获取所有的sku
        List<SkuEntity> skuEntities = this.skuMapper.selectList(new QueryWrapper<SkuEntity>().eq("spu_id", spuId));
        if (CollectionUtils.isEmpty(skuEntities)) {
            return null;
        }
        //收集sku的id集合
        List<Long> skuIds = skuEntities.stream().map(SkuEntity::getId).collect(Collectors.toList());

        //获取sku对应的参数值和skuId的映射
        List<Map<String, Object>> skuMappingList = this.baseMapper.querySkuAttrMappingSkuId(skuIds);
        //将skuMappingList转化为{黑色、8G、128G:8} 样式的map集合
        if (CollectionUtils.isEmpty(skuMappingList)) {
            return null;
        }
        Map<String, Long> skuMappingMap = skuMappingList.stream().collect(Collectors.toMap(skuMapping -> skuMapping.get("attrJson").toString(), skuMapping -> (Long) skuMapping.get("sku_id")));
        return JSON.toJSONString(skuMappingMap);
    }
}

