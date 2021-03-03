package com.atguigu.gmall.pms.controller;

import java.io.FileNotFoundException;
import java.util.List;

import com.atguigu.gmall.pms.vo.SpuVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atguigu.gmall.pms.entity.SpuEntity;
import com.atguigu.gmall.pms.service.SpuService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.bean.PageParamVo;

/**
 * spu信息
 *
 * @author hehao
 * @email hehao@hehao.com
 * @date 2021-01-18 18:30:53
 */
@Api(tags = "spu信息 管理")
@RestController
@RequestMapping("pms/spu")
public class SpuController {

    @Autowired
    private SpuService spuService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("category/{categoryId}")
    @ApiOperation("带有条件的分页查询")
    public ResponseVo<PageResultVo> querySpuPageByKey(
            @PathVariable("categoryId") Long cid, PageParamVo paramVo) {
        PageResultVo pageResultVo = spuService.querySpuPageByKey(cid, paramVo);
        return ResponseVo.ok(pageResultVo);
    }

    /**
     * 列表
     */
    @PostMapping("/searchSpu")
    @ApiOperation("分页查询")
    public ResponseVo<List<SpuEntity>> querySpuByPage(@RequestBody PageParamVo paramVo) {
        PageResultVo pageResultVo = spuService.queryPage(paramVo);
        List<SpuEntity> resultVoList = (List<SpuEntity>) pageResultVo.getList();
        return ResponseVo.ok(resultVoList);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<SpuEntity> querySpuById(@PathVariable("id") Long id) {
        SpuEntity spu = spuService.getById(id);

        return ResponseVo.ok(spu);
    }

    /**
     * Request URL: http://api.gmall.com/pms/spu
     * Request Method: POST
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody SpuVo spuVo) {
        this.spuService.bigSave(spuVo);
        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody SpuEntity spu) {
        spuService.updateById(spu);
        //修改spu时，发送rabbitmq消息通知购物车系统更新价格缓存
        this.rabbitTemplate.convertAndSend("CART_PRICE_EXCHANGE","price.update",spu.getId());
        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids) {
        spuService.removeByIds(ids);

        return ResponseVo.ok();
    }

    /**
     * 根据skuId获取spu信息
     */
    @GetMapping("/sku/{skuId}")
    @ApiOperation("详情查询")
    public ResponseVo<SpuEntity> querySpuBySkuId(@PathVariable("skuId") Long skuId) {
        SpuEntity spu = spuService.querySpuBySkuId(skuId);
        return ResponseVo.ok(spu);
    }

}
