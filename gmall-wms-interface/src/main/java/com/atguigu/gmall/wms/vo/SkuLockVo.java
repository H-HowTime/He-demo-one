package com.atguigu.gmall.wms.vo;

import lombok.Data;

/**
 * @author hehao
 * @create 2021-02-27 20:41
 */
@Data
public class SkuLockVo {

    private Long skuId; //skuId
    private Integer count;//购买数量
    private Boolean lock;//锁定状态，是否有库存
    private Long wareId;//记录锁库存对应的库存id
}
