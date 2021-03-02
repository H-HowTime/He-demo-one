package com.atguigu.gmall.search.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.List;

/**
 * @author hehao
 * @create 2021-01-27 17:18
 */
@Data
@Document(indexName = "goods",type = "info",shards = 3,replicas = 2)
public class Goods {

    //商品类表搜索字段 v
    @Id
    private Long skuId;
    @Field(name = "price",index = true,type = FieldType.Double)
    private Double price;
    @Field(name = "title",index = true,type = FieldType.Text,analyzer = "ik_max_word")
    private String title;
    @Field(name = "sbuTitle",index = false,type = FieldType.Keyword)
    private String sbuTitle;
    @Field(name = "defaultImage",index = false,type = FieldType.Keyword)
    private String defaultImage;

    //排序所需字段
    @Field(name = "sales",index = true,type = FieldType.Long)
    private Long sales = 0l; //销量
    @Field(name = "createTime",index = true,type = FieldType.Date)
    private Date createTime; //spu创建时间

    //过滤所需库存字段
    @Field(name = "store",index = true,type = FieldType.Boolean)
    private Boolean store = false;

    //品牌聚合所需字段
    @Field(name = "brandId",index = true,type = FieldType.Long)
    private Long brandId;
    @Field(name = "brandName",index = true,type = FieldType.Keyword)
    private String brandName; //品牌名称
    @Field(name = "logo",index = true,type = FieldType.Keyword)
    private String logo; //品牌图标

    //分类聚合所需字段
    @Field(name = "categoryId",index = true,type = FieldType.Long)
    private Long categoryId;
    @Field(name = "categoryName",index = true,type = FieldType.Keyword)
    private String categoryName;

    //规格参数聚合所需字段
    @Field(name = "searchAttrs",index = true,type = FieldType.Nested) //使用nested类型可以防止数据扁平化
    private List<SearchAttrValue> searchAttrs;
}
