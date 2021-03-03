package com.atguigu.gmall.search.service;

import com.atguigu.gmall.search.pojo.SearchRequestParamVo;
import com.atguigu.gmall.search.pojo.SearchResponseAttrVo;
import com.atguigu.gmall.search.pojo.SearchResponseVo;

/**
 * @author hehao
 * @create 2021-01-29 18:14
 */
public interface SearchService {
    SearchResponseVo  search(SearchRequestParamVo searchRequestParamVo);
}
