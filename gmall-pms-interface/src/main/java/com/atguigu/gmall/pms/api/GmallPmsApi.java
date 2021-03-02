package com.atguigu.gmall.pms.api;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.atguigu.gmall.pms.vo.SaleAttrVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author hehao
 * @create 2021-01-27 18:45
 */
public interface GmallPmsApi {

    @PostMapping("pms/spu/searchSpu")
    @ApiOperation("分页查询")
    public ResponseVo<List<SpuEntity>> querySpuByPage(@RequestBody PageParamVo paramVo);

    @GetMapping("pms/spu/{id}")
    @ApiOperation("详情查询")
    public ResponseVo<SpuEntity> querySpuById(@PathVariable("id") Long id);

    @GetMapping("pms/sku/spu/{spuId}")
    @ApiOperation("spu对应的所有sku详情查询")
    public ResponseVo<List<SkuEntity>> querySkuBySpuId(@PathVariable("spuId") Long spuId);

    @GetMapping("pms/brand/{id}")
    @ApiOperation("详情查询")
    public ResponseVo<BrandEntity> queryBrandById(@PathVariable("id") Long id);

    @GetMapping("pms/category/{id}")
    @ApiOperation("详情查询")
    public ResponseVo<CategoryEntity> queryCategoryById(@PathVariable("id") Long id);

    @GetMapping("pms/category/parent/{parentId}")
    @ApiOperation("分类查询")
    public ResponseVo<List<CategoryEntity>> queryCategoryByPid(@PathVariable("parentId") Long pid);

    @GetMapping("pms/category/level2/{parentId}")
    @ApiOperation("分类查询")
    public ResponseVo<List<CategoryEntity>> getLv2CategoriesByPid(@PathVariable("parentId") Long pid);

    @GetMapping("pms/spuattrvalue/search/{spuId}")
    @ApiOperation("详情查询")
    public ResponseVo<List<SpuAttrValueEntity>> querySpuAttrValueBySpuIdAndCategoryId(
            @PathVariable("spuId") Long spuId,
            @RequestParam("categoryId") Long categoryId);

    @GetMapping("pms/skuattrvalue/search/{skuId}")
    @ApiOperation("详情查询")
    public ResponseVo<List<SkuAttrValueEntity>> querySkuAttrValueBySkuIdAndCategoryId(
            @PathVariable("skuId") Long skuId,
            @RequestParam("categoryId") Long categoryId);

    /**
     * 根据skuid获取sku信息
     */
    @GetMapping("pms/sku/{id}")
    @ApiOperation("详情查询")
    public ResponseVo<SkuEntity> querySkuById(@PathVariable("id") Long id);

    /**
     * 根据cid获取123级分类
     */
    @GetMapping("pms/category/all/{cid}")
    @ApiOperation("查询")
    public ResponseVo<List<CategoryEntity>> query123CategoryByCid(@PathVariable("cid") Long cid);

    /**
     * 根据skuId获取spu信息
     */
    @GetMapping("pms/spu/sku/{skuId}")
    @ApiOperation("详情查询")
    public ResponseVo<SpuEntity> querySpuBySkuId(@PathVariable("skuId") Long skuId);

    /**
     * 根据skuId获取sku图片信息
     */
    @GetMapping("pms/skuimages/sku/{skuId}")
    @ApiOperation("详情查询")
    public ResponseVo<List<SkuImagesEntity>> querySkuImagesBySkuId(@PathVariable("skuId") Long skuId);

    /**
     * 根据spuId获取spu下的所有sku信息以及对应的规格参数值
     */
    @GetMapping("pms/skuattrvalue/sku/{spuId}")
    @ApiOperation("详情查询")
    public ResponseVo<List<SaleAttrVo>> querySaleAttrsBySpuId(@PathVariable("spuId") Long spuId);

    /**
     * 根据skuId获取对应的规格参数值
     */
    @GetMapping("pms/skuattrvalue/skuAttr/{skuId}")
    @ApiOperation("详情查询")
    public ResponseVo<List<SkuAttrValueEntity>> querySkuAttrValueBySkuId(@PathVariable("skuId") Long skuId);

    /**
     * 根据spuId获取所有对应的sku规格参数值和skuid的映射关系{黑色、8G、128G:8}
     */
    @GetMapping("pms/skuattrvalue/skuAttrMappingSkuId/{spuId}")
    @ApiOperation("详情查询")
    public ResponseVo<String> querySkuAttrMappingSkuId(@PathVariable("spuId") Long spuId);

    /**
     * 根据spuId获取商品描述信息
     */
    @GetMapping("pms/spudesc/{spuId}")
    @ApiOperation("详情查询")
    public ResponseVo<SpuDescEntity> querySpuDescById(@PathVariable("spuId") Long spuId);

    /**
     * 根据categoryId获取分组信息，再根据spuId和skuId获取对应的规格参数和值
     */
    @GetMapping("pms/attrgroup/spu/sku/{categoryId}")
    @ApiOperation("详情查询")
    public ResponseVo<List<ItemGroupVo>> queryAttrGroupValueByCidAndSpuIdAndSkuId(
            @PathVariable("categoryId") Long categoryId,
            @RequestParam("spuId") Long spuId,
            @RequestParam("skuId") Long skuId);

}
