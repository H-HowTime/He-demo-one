package com.atguigu.gmall.item.controller;

import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.item.vo.ItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author hehao
 * @create 2021-02-19 23:37
 */
@Controller
public class ItemController {

    @Autowired
    private ItemService itemService;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @GetMapping("{skuId}.html")
    public String queryItem(@PathVariable("skuId") Long skuId, Model model) {
        ItemVo itemVo = itemService.queryItem(skuId);
        //生成静态页面
        threadPoolExecutor.execute(() -> {
            itemService.buildHtml(itemVo);
        });
        model.addAttribute("itemVo", itemVo);
        return "item";
    }

}
