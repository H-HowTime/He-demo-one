package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.mapper.SpuAttrValueMapper;
import com.atguigu.gmall.pms.service.AttrService;
import com.atguigu.gmall.pms.vo.GroupAttrVo;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.AttrGroupMapper;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;
import org.springframework.util.CollectionUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupMapper, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrMapper attrMapper;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    private SpuAttrValueMapper spuAttrValueMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<AttrGroupEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageResultVo(page);
    }


    @Override
    public List<AttrGroupEntity> queryWithAttrsByCid(long cid) {
        //能不使用联查就不使用联查
        //先查询参数规格分组
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("category_id", cid);
        List<AttrGroupEntity> attrGroupEntities = baseMapper.selectList(wrapper);
        if (CollectionUtils.isEmpty(attrGroupEntities)) {
            return null;
        }
        //遍历分组，查询属性
//        for (AttrGroupEntity groupEntity : attrGroupEntities) {
//            QueryWrapper<AttrEntity> wrapper1 = new QueryWrapper<>();
//            wrapper1.eq("group_id", groupEntity.getId()).eq("type",1);
//            List<AttrEntity> attrEntities = attrMapper.selectList(wrapper1);
//            groupEntity.setAttrEntities(attrEntities);
//        }
        attrGroupEntities.forEach(groupEntity -> {
            QueryWrapper<AttrEntity> wrapper1 = new QueryWrapper<>();
            wrapper1.eq("group_id", groupEntity.getId()).eq("type", 1);
            List<AttrEntity> attrEntities = attrMapper.selectList(wrapper1);
            groupEntity.setAttrEntities(attrEntities);
        });
        return attrGroupEntities;
    }

    @Override
    public List<ItemGroupVo> queryAttrGroupValueByCidAndSpuIdAndSkuId(Long categoryId, Long spuId, Long skuId) {
        //根据categoryId获取分组信息
        List<AttrGroupEntity> attrGroupEntities = this.baseMapper.selectList(new QueryWrapper<AttrGroupEntity>().eq("category_id", categoryId));
        if (CollectionUtils.isEmpty(attrGroupEntities)) {
            return null;
        }
        List<ItemGroupVo> itemGroupVos = attrGroupEntities.stream().map(attrGroupEntity -> {
            ItemGroupVo itemGroupVo = new ItemGroupVo();
            itemGroupVo.setGroupId(attrGroupEntity.getId());
            itemGroupVo.setGroupName(attrGroupEntity.getName());
            //获取规格参数对应的值
            //根据group_id获取对应的attrIds
            List<AttrEntity> attrs = attrMapper.selectList(new QueryWrapper<AttrEntity>().eq("group_id", attrGroupEntity.getId()));
            if (!CollectionUtils.isEmpty(attrs)) {
                List<Long> attrIds = attrs.stream().map(AttrEntity::getId).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(attrIds)) {
                    List<GroupAttrVo> groupAttrVos = new ArrayList<>();
                    List<SkuAttrValueEntity> skuAttrValueEntities = this.skuAttrValueMapper.selectList(new QueryWrapper<SkuAttrValueEntity>().in("attr_id", attrIds).eq("sku_id", skuId));
                    if(!CollectionUtils.isEmpty(skuAttrValueEntities)){
                        groupAttrVos.addAll(skuAttrValueEntities.stream().map(skuAttrValueEntity -> {
                                    GroupAttrVo groupAttrVo = new GroupAttrVo();
                                    BeanUtils.copyProperties(skuAttrValueEntity, groupAttrVo);
                                    return groupAttrVo;
                                }
                        ).collect(Collectors.toList()));
                    }
                    List<SpuAttrValueEntity> spuAttrValueEntities
                            = this.spuAttrValueMapper.selectList(new QueryWrapper<SpuAttrValueEntity>().in("attr_id", attrIds).eq("spu_id", spuId));
                    if(!CollectionUtils.isEmpty(spuAttrValueEntities)){

                        groupAttrVos.addAll(spuAttrValueEntities.stream().map(spuAttrValueEntity -> {
                                    GroupAttrVo groupAttrVo = new GroupAttrVo();
                                    BeanUtils.copyProperties(spuAttrValueEntity, groupAttrVo);
                                    return groupAttrVo;
                                }
                        ).collect(Collectors.toList()));
                    }
                    itemGroupVo.setAttrValues(groupAttrVos);
                }
            }
            return itemGroupVo;
        }).collect(Collectors.toList());
        return itemGroupVos;
    }
}