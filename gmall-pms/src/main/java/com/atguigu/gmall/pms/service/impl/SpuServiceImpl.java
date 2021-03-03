package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.feign.GmallSmsFeignClient;
import com.atguigu.gmall.pms.mapper.SkuImagesMapper;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pms.mapper.SpuDescMapper;
import com.atguigu.gmall.pms.service.*;
import com.atguigu.gmall.pms.vo.SkuVo;
import com.atguigu.gmall.pms.vo.SpuAttrValueVo;
import com.atguigu.gmall.pms.vo.SpuVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import com.sun.corba.se.spi.orbutil.threadpool.WorkQueue;
import com.sun.org.apache.bcel.internal.generic.NEW;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SpuMapper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {

    @Autowired
    private SpuDescMapper spuDescMapper;

    @Autowired
    private SpuAttrValueService attrValueService;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuAttrValueService skuAttrValueService;

    @Autowired
    private GmallSmsFeignClient smsFeignClient;

    @Autowired
    private SpuDescService spuDescService;

    @Autowired
    private SpuAttrValueService spuAttrValueService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public PageResultVo querySpuPageByKey(Long cid, PageParamVo paramVo) {
        QueryWrapper<SpuEntity> wrapper = new QueryWrapper<>();
        //当cid=0时查询全站
        if (cid != 0) {
            wrapper.eq("category_id", cid);
        }
        //查询条件
        String key = paramVo.getKey();
        if (StringUtils.isNotBlank(key)) {
            //import org.apache.commons.lang3.StringUtils; isBlank()方法去除空格比较
            wrapper.and(t -> t.eq("id", key).or().like("name", key));
            //SELECT * FROM pms_spu WHERE cid = XX AND (id = xx OR name like "%xx%")
            //使用and(Consumer<Param> consumer)的and方法进行拼接小括号
        }
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(), wrapper
        );
        return new PageResultVo(page);
    }

    @GlobalTransactional
    @Override
    public void bigSave(SpuVo spuVo) {
        //1、先保存spu信息
        //1.1、保存spu信息-- pms_spu
        Long spuId = saveSpu(spuVo);
        //1.2、保存spu描述信息--pms_spu_decr
        this.spuDescService.saveSpuDecr(spuVo, spuId);
        //1.3、保存基本属性信息--pms_spu_attr_value
        this.spuAttrValueService.saveSpuAttrValue(spuVo, spuId);

//        new FileInputStream("xxx");

//        try {
//            TimeUnit.SECONDS.sleep(4);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        //2、保存sku信息
        this.saveSkuInfo(spuVo, spuId);

        //使用rabbitmq消息队列来保存elasticsearch数据
        this.rabbitTemplate.convertAndSend("PMS_ITEM_EXCHANGE", "item.insert", spuId);
    }

    @Override
    public SpuEntity querySpuBySkuId(Long skuId) {
        //根据skuId先获取spuId
        SkuEntity skuEntity = this.skuMapper.selectOne(new QueryWrapper<SkuEntity>().eq("id", skuId));
        if (skuEntity == null) {
            return null;
        }
        return this.baseMapper.selectById(skuEntity.getSpuId());
    }

    /**
     * 保存sku信息
     * 远程调用保存sku营销优惠信息
     *
     * @param spuVo
     * @param spuId
     */
    private void saveSkuInfo(SpuVo spuVo, Long spuId) {
        List<SkuVo> skuVos = spuVo.getSkus();
        if (!CollectionUtils.isEmpty(skuVos)) {
            skuVos.forEach(skuVo -> {
                //2.1、保存sku信息--pms_sku
                SkuEntity sku = new SkuEntity();
                BeanUtils.copyProperties(skuVo, sku);
                sku.setSpuId(spuId);
                sku.setCategoryId(spuVo.getCategoryId());
                sku.setBrandId(spuVo.getBrandId());
                List<String> images = skuVo.getImages();
                if (!CollectionUtils.isEmpty(images)) {
                    sku.setDefaultImage(StringUtils.isNotBlank(skuVo.getDefaultImage()) ? skuVo.getDefaultImage() : images.get(0));
                }
                this.skuMapper.insert(sku);
                //2.2、保存sku图片--pms_sku_images
                if (!CollectionUtils.isEmpty(images)) {
                    this.skuImagesService.saveBatch(images.stream().map(image -> {
                        SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                        skuImagesEntity.setSkuId(sku.getId());
                        skuImagesEntity.setUrl(image);
                        skuImagesEntity.setDefaultStatus(StringUtils.equals(sku.getDefaultImage(), image) ? 1 : 0);
                        return skuImagesEntity;
                    }).collect(Collectors.toList()));
                }
                //2.3、保存销售属性信息--pms_sku_attr_value
                List<SkuAttrValueEntity> saleAttrs = skuVo.getSaleAttrs();
                if (!CollectionUtils.isEmpty(saleAttrs)) {
                    saleAttrs.forEach(saleAttr ->
                            saleAttr.setSkuId(sku.getId())
                    );
                    this.skuAttrValueService.saveBatch(saleAttrs);
                }
                //3、保存营销信息 -- 远程调用gmall-sms服务
                //3.1、保存积分优惠信息--sms_sku_bounds
                //3.2、保存满减优惠信息--sms_sku_full_reduction
                //3.3、保存打折优惠信息--sms_sku_ladder
                SkuSaleVo skuSaleVo = new SkuSaleVo();
                BeanUtils.copyProperties(skuVo, skuSaleVo);
                skuSaleVo.setId(sku.getId());
                this.smsFeignClient.saveSalesBySkuId(skuSaleVo);
            });
        }
    }

    /**
     * 保存spu信息-- pms_spu
     *
     * @param spuVo
     * @return
     */
    private Long saveSpu(SpuVo spuVo) {
        SpuEntity spu = new SpuEntity();
        BeanUtils.copyProperties(spuVo, spu);
        spu.setCreateTime(new Date());
        spu.setUpdateTime(spu.getCreateTime());
        this.baseMapper.insert(spu);
        return spu.getId();
    }

}