package com.atguigu.gmall.pms.controller;

import java.util.List;

import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import com.atguigu.gmall.pms.vo.SaleAttrVo;
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

import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.bean.PageParamVo;

/**
 * sku销售属性&值
 *
 * @author hehao
 * @email hehao@hehao.com
 * @date 2021-01-18 18:30:53
 */
@Api(tags = "sku销售属性&值 管理")
@RestController
@RequestMapping("pms/skuattrvalue")
public class SkuAttrValueController {

    @Autowired
    private SkuAttrValueService skuAttrValueService;

    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> querySkuAttrValueByPage(PageParamVo paramVo){
        PageResultVo pageResultVo = skuAttrValueService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<SkuAttrValueEntity> querySkuAttrValueById(@PathVariable("id") Long id){
        SkuAttrValueEntity skuAttrValue = skuAttrValueService.getById(id);

        return ResponseVo.ok(skuAttrValue);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody SkuAttrValueEntity skuAttrValue){
		skuAttrValueService.save(skuAttrValue);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody SkuAttrValueEntity skuAttrValue){
		skuAttrValueService.updateById(skuAttrValue);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids){
		skuAttrValueService.removeByIds(ids);

        return ResponseVo.ok();
    }

    @GetMapping("search/{skuId}")
    @ApiOperation("详情查询")
    public ResponseVo<List<SkuAttrValueEntity>> querySkuAttrValueBySkuIdAndCategoryId(
            @PathVariable("skuId") Long skuId,
            @RequestParam("categoryId") Long categoryId) {
        List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrValueService.querySkuAttrValueBySkuIdAndCategoryId(skuId,categoryId);

        return ResponseVo.ok(skuAttrValueEntities);
    }

    /**
     * 根据spuId获取spu下的所有sku信息以及对应的规格参数值
     */
    @GetMapping("/sku/{spuId}")
    @ApiOperation("详情查询")
    public ResponseVo<List<SaleAttrVo>> querySaleAttrsBySpuId(@PathVariable("spuId") Long spuId) {
        List<SaleAttrVo> saleAttrVos = this.skuAttrValueService.querySaleAttrsBySpuId(spuId);

        return ResponseVo.ok(saleAttrVos);
    }

    /**
     * 根据skuId获取对应的规格参数值
     */
    @GetMapping("/skuAttr/{skuId}")
    @ApiOperation("详情查询")
    public ResponseVo<List<SkuAttrValueEntity>> querySkuAttrValueBySkuId(@PathVariable("skuId") Long skuId) {
        List<SkuAttrValueEntity> skuAttrValueEntityList = this.skuAttrValueService.querySkuAttrValueBySkuId(skuId);
        return ResponseVo.ok(skuAttrValueEntityList);
    }

    /**
     * 根据spuId获取所有对应的sku规格参数值和skuid的映射关系{黑色、8G、128G:8}
     */
    @GetMapping("/skuAttrMappingSkuId/{spuId}")
    @ApiOperation("详情查询")
    public ResponseVo<String> querySkuAttrMappingSkuId(@PathVariable("spuId") Long spuId) {
        String mapping =  this.skuAttrValueService.querySkuAttrMappingSkuId(spuId);
        System.out.println(mapping);
        return ResponseVo.ok(mapping);
    }

}
