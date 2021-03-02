package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.vo.SpuAttrValueVo;
import com.atguigu.gmall.pms.vo.SpuVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SpuAttrValueMapper;
import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import com.atguigu.gmall.pms.service.SpuAttrValueService;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


@Service("spuAttrValueService")
public class SpuAttrValueServiceImpl extends ServiceImpl<SpuAttrValueMapper, SpuAttrValueEntity> implements SpuAttrValueService {

    @Autowired
    private AttrMapper attrMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuAttrValueEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuAttrValueEntity>()
        );

        return new PageResultVo(page);
    }

    /**
     * 保存基本属性信息--pms_spu_attr_value
     *
     * @param spuVo
     * @param spuId
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveSpuAttrValue(SpuVo spuVo, Long spuId) {
        List<SpuAttrValueVo> baseAttrs = spuVo.getBaseAttrs();
        if (!CollectionUtils.isEmpty(baseAttrs)) {
            //使用java8新特性Stream()API来进行数组之间的转换
            this.saveBatch(baseAttrs.stream().map(baseAttr -> {
                SpuAttrValueEntity spuAttrValue = new SpuAttrValueEntity();
                BeanUtils.copyProperties(baseAttr, spuAttrValue);
                spuAttrValue.setSpuId(spuId);
                return spuAttrValue;
            }).collect(Collectors.toList()));
        }
    }

    @Override
    public List<SpuAttrValueEntity> querySpuAttrValueBySpuIdAndCategoryId(Long spuId, Long categoryId) {
        //根据spuId和categoryId 获取attr信息
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("category_id",categoryId);
        wrapper.eq("search_type",1);
        List<AttrEntity> attrEntities = attrMapper.selectList(wrapper);
        if(CollectionUtils.isEmpty(attrEntities)){
            return null;
        }
        List<Long> ids = attrEntities.stream().map(AttrEntity::getId).collect(Collectors.toList());
        return this.list(new QueryWrapper<SpuAttrValueEntity>().eq("spu_id",spuId).in("attr_id",ids));
    }
}