package com.atguigu.gmall.wms.mapper;

import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 *
 * @author hehao
 * @email hehao@hehao.com
 * @date 2021-01-18 23:39:23
 */
@Mapper
public interface WareSkuMapper extends BaseMapper<WareSkuEntity> {

    Integer updateLockStock(@Param("count") Integer count, @Param("wareId") Long wareId);

    List<WareSkuEntity> selectStock(@Param("skuId") Long skuId, @Param("count") Integer count);

    Integer unLockStock(@Param("count") Integer count, @Param("wareId") Long wareId);

    Integer minusStock(@Param("count") Integer count, @Param("wareId") Long wareId);
}
