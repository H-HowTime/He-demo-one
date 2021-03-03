package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.vo.SpuVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SpuDescMapper;
import com.atguigu.gmall.pms.entity.SpuDescEntity;
import com.atguigu.gmall.pms.service.SpuDescService;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


@Service("spuDescService")
public class SpuDescServiceImpl extends ServiceImpl<SpuDescMapper, SpuDescEntity> implements SpuDescService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuDescEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuDescEntity>()
        );

        return new PageResultVo(page);
    }

    /**
     * 保存spu描述信息--pms_spu_decr
     *
     * @param spuVo
     * @param spuId
     */
//    @Transactional //使用@Transactional注解的方法必须是可重写的
    public void saveSpuDecr(SpuVo spuVo, Long spuId) {
        if (!CollectionUtils.isEmpty(spuVo.getSpuImages())) {
            //当spuImages不为空的时转化为以逗号分隔的字符串保存
            SpuDescEntity spuDesc = new SpuDescEntity();
            spuDesc.setSpuId(spuId);
            spuDesc.setDecript(StringUtils.join(spuVo.getSpuImages(), ","));
            this.baseMapper.insert(spuDesc);
        }
    }
}