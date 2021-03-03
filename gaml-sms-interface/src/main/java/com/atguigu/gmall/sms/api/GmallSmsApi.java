package com.atguigu.gmall.sms.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author hehao
 * @create 2021-01-20 20:58
 */
public interface GmallSmsApi {
    @PostMapping("sms/skubounds/save/sale")
    @ApiOperation("保存")
    public ResponseVo<Object> saveSalesBySkuId(@RequestBody SkuSaleVo skuSaleVo);

    /**
     * 根据skuId获取促销信息-- 积分、打折、满减
     */
    @GetMapping("sms/skubounds/sku/{skuId}")
    @ApiOperation("根据skuId获取促销信息-- 积分、打折、满减")
    public ResponseVo<List<ItemSaleVo>> promotionBySkuId(@PathVariable("skuId") Long skuId);
}
