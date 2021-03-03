package com.atguigu.gmall.item.vo;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.SkuImagesEntity;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.atguigu.gmall.pms.vo.SaleAttrVo;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author hehao
 * @create 2021-02-19 11:27
 */
@Data
public class ItemVo {

    //面包屑：三级分类信息 v
    private List<CategoryEntity> categories;

    //面包屑：品牌 v
    private Long brandId;
    private String brandName;

    //面包屑：spu信息 v
    private Long spuId;
    private String spuName;

    //sku信息 v
    private Long skuId;
    private String title;
    private String subTitle;
    private BigDecimal price;
    private Integer weight;
    private String defaultImage;

    //sku图片列表 v
    private List<SkuImagesEntity> skuImages;

    //库存信息 v
    private Boolean store = false; //默认无货

    //促销信息 v
    private List<ItemSaleVo> sales;

    //[{attrId:3,attrName:'机身颜色',attrValue:['黑色','白色']},
    //{attrId:4,attrName:'运行内存',attrValue:'['8G','16G']},
    // {attrId:5,attrName:'机身存储',attrValue:['64G','128G']},]
    //销售属性信息--当前sku对应的spu下的所有sku信息
    private List<SaleAttrVo> saleAttrs;

    // {3:'黑色',4:'8G',5:'64G'}
    //当前sku对应的属性信息
    private Map<Long ,String> saleAttr;

    //销售属性组合和skuId的映射关系 -- {黑色、8G、128G:8}
    private String skuJsons;

    //商品的海报信息
    private List<String> spuImages;

    //规格参数分组列表
    private List<ItemGroupVo> groups;

}
