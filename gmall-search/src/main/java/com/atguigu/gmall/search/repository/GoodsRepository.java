package com.atguigu.gmall.search.repository;

import com.atguigu.gmall.search.pojo.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @author hehao
 * @create 2021-01-27 18:13
 */
public interface GoodsRepository extends ElasticsearchRepository<Goods,Long> {
}
