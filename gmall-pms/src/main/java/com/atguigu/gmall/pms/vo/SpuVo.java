package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SpuDescEntity;
import com.atguigu.gmall.pms.entity.SpuEntity;
import lombok.Data;

import java.util.List;

/**
 * @author hehao
 * @create 2021-01-20 13:01
 */
@Data
public class SpuVo extends SpuEntity {
    //继承spu--spu的相关信息
    //spu描述信息
    private List<String> spuImages;

    //基本属性
    private List<SpuAttrValueVo> baseAttrs;

    //sku信息
    private List<SkuVo> skus;

}
