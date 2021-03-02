package com.atguigu.gmall.item.service;

import com.atguigu.gmall.item.vo.ItemVo;

import java.io.IOException;

/**
 * @author hehao
 * @create 2021-02-19 23:45
 */
public interface ItemService {
    ItemVo queryItem(Long skuId);

    void buildHtml(ItemVo itemVo) ;
}
