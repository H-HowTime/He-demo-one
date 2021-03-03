package com.atguigu.gmall.sms.controller;

import java.util.List;

import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.gmall.sms.service.SkuBoundsService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.bean.PageParamVo;

/**
 * 商品spu积分设置
 *
 * @author hehao
 * @email hehao@hehao.com
 * @date 2021-01-18 23:15:04
 */
@Api(tags = "商品spu积分设置 管理")
@RestController
@RequestMapping("sms/skubounds")
public class SkuBoundsController {

    @Autowired
    private SkuBoundsService skuBoundsService;


    /**
     * pms远程调用保存营销信息 feign远程调用只能通过Post请求接收json对象
     */
    @PostMapping("save/sale")
    @ApiOperation("保存")
    public ResponseVo<Object> saveSalesBySkuId(@RequestBody SkuSaleVo skuSaleVo) {
        skuBoundsService.saveSalesBySkuId(skuSaleVo);
        return ResponseVo.ok();
    }

    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> querySkuBoundsByPage(PageParamVo paramVo) {
        PageResultVo pageResultVo = skuBoundsService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<SkuBoundsEntity> querySkuBoundsById(@PathVariable("id") Long id) {
        SkuBoundsEntity skuBounds = skuBoundsService.getById(id);

        return ResponseVo.ok(skuBounds);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody SkuBoundsEntity skuBounds) {
        skuBoundsService.save(skuBounds);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody SkuBoundsEntity skuBounds) {
        skuBoundsService.updateById(skuBounds);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids) {
        skuBoundsService.removeByIds(ids);

        return ResponseVo.ok();
    }

    /**
     * 根据skuId获取促销信息-- 积分、打折、满减
     */
    @GetMapping("sku/{skuId}")
    @ApiOperation("分页查询")
    public ResponseVo<List<ItemSaleVo>> promotionBySkuId(@PathVariable("skuId") Long skuId) {
        List<ItemSaleVo> itemSaleVos = skuBoundsService.promotionBySkuId(skuId);
        return ResponseVo.ok(itemSaleVos);
    }

}
