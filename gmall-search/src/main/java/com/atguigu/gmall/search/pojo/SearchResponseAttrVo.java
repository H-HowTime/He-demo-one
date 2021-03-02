package com.atguigu.gmall.search.pojo;

import lombok.Data;

import java.util.List;

/**
 * @author hehao
 * @create 2021-01-29 21:40
 */
@Data
public class SearchResponseAttrVo {

    //属性id
    private Long attrId;

    //属性名称
    private String attrName;

    //属性id对应的可选值列表
    private List<String> attrValues;
}
