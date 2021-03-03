package com.atguigu.gmall.pms.vo;

import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * @author hehao
 * @create 2021-02-19 13:05
 */
@Data
public class SaleAttrVo {

    //{attrId:3,attrName:'机身颜色',attrValue:['黑色','白色']}
    private Long attrId;
    private String attrName;
    private Set<String> attrValues;
}
