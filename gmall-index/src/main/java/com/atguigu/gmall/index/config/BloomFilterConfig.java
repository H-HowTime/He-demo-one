package com.atguigu.gmall.index.config;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.feign.GmallPmsFeignClient;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author hehao
 * @create 2021-02-05 23:15
 */
@Configuration
public class BloomFilterConfig {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private GmallPmsFeignClient gmallPmsFeignClient;

    private static final String KEY_PREFIX = "index:cates";

    @Bean
    public RBloomFilter rBloomFilter() {
        //初始化布隆过滤器
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter("index:bloom");
        bloomFilter.tryInit(3000l, 0.01d);
        //将分类数据保存到布隆过滤器中
        ResponseVo<List<CategoryEntity>> queryCategoryByPidVo = this.gmallPmsFeignClient.queryCategoryByPid(0l);
        List<CategoryEntity> categoryEntities = queryCategoryByPidVo.getData();
        if (!CollectionUtils.isEmpty(categoryEntities)) {

            categoryEntities.forEach(categoryEntity -> {
                bloomFilter.add(KEY_PREFIX + "[" + categoryEntity.getId() + "]");
            });
        }
        return bloomFilter;
    }
}
