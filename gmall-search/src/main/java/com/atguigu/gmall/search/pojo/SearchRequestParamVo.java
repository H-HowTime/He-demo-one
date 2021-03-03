package com.atguigu.gmall.search.pojo;

import lombok.Data;

import java.util.List;

/**
 * @author hehao
 * @create 2021-01-29 17:31
 *
 * search.gmall.com/Search?keyword="手机"&brandId=1,2,3&categoryId=225&startPrice=1000.0&endPrice=20000.0
 *  &props=4:6G-8G,5:128G-256G&store=false&pageNum=1&pageSize=20
 */
@Data
public class SearchRequestParamVo {

    //检索关键字
    private String keyword;

    //品牌的过滤条件
    private List<Long> brandId; //brandId=1,2,3使用，分隔可以使用list集合接收

    //分类的过滤条件
    private List<Long> categoryId;

    //价格的过滤条件
    private Double startPrice; //起始价格
    private Double endPrice; //终止价格价格

    //规格参数的过滤条件 ["4:6G-8G","5:128G-256G"]
    private List<String> props;

    //库存过滤条件
    private Boolean store = false;

    //分页数据
    private Integer pageNum = 1;
    private final Integer pageSize = 20; //每页显示多少条数据不能修改，要写死

    //排序 默认0-得分排序，1-价格升序，2-价格降序，3-销量降序，4-新品降序
    private Integer sort = 0;

}
