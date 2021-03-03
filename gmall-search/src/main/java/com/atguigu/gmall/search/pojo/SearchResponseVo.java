package com.atguigu.gmall.search.pojo;

import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import lombok.Data;

import java.util.List;

/**
 * @author hehao
 * @create 2021-01-29 21:31
 */
@Data
public class SearchResponseVo {

    //品牌列表
    private List<BrandEntity> brands;

    //分类列表
    private List<CategoryEntity> categories;

    //规格参数列表
    private List<SearchResponseAttrVo> searchAttrs;

    //分页数据
    private Integer pageNum;
    private Integer pageSize;

    //总记录数
    private Long total;

    //商品列表数据
    private List<Goods> goodsList;
}
