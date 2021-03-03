package com.atguigu.gmall.pms.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author hehao
 * @create 2021-01-20 20:26
 */
@Data
public class SkuSaleVo {
    //skuId
    private Long id;

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
}
