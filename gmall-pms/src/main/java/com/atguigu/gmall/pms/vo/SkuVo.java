package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author hehao
 * @create 2021-01-20 16:53
 */
@Data
public class SkuVo extends SkuEntity {
    //继承sku--sku的相关属性
    //库存信息
    private Integer stock;

    //积分优惠信息
    private BigDecimal growBounds;
    private BigDecimal buyBounds;
    private List<Integer> work;

    //满减优惠
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private Integer fullAddOther;

    //打折优惠
    private Integer fullCount;
    private BigDecimal discount;
    private Integer ladderAddOther;

    //sku图片
    private List<String> images;

    //销售属性
    private List<SkuAttrValueEntity> saleAttrs;

}
