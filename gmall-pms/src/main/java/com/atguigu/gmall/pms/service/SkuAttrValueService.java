package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.vo.SaleAttrVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;

import java.util.List;
import java.util.Map;

/**
 * sku销售属性&值
 *
 * @author hehao
 * @email hehao@hehao.com
 * @date 2021-01-18 18:30:53
 */
public interface SkuAttrValueService extends IService<SkuAttrValueEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    List<SkuAttrValueEntity> querySkuAttrValueBySkuIdAndCategoryId(Long skuId, Long categoryId);

    List<SaleAttrVo> querySaleAttrsBySpuId(Long spuId);

    List<SkuAttrValueEntity> querySkuAttrValueBySkuId(Long skuId);

    String querySkuAttrMappingSkuId(Long spuId);
}

