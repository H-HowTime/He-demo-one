package com.atguigu.gmall.search.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.search.pojo.SearchRequestParamVo;
import com.atguigu.gmall.search.pojo.SearchResponseAttrVo;
import com.atguigu.gmall.search.pojo.SearchResponseVo;
import com.atguigu.gmall.search.service.SearchService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author hehao
 * @create 2021-01-29 18:13
 */
@Controller
@RequestMapping("search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping
    @ApiOperation("商品搜索")
    public String search(SearchRequestParamVo searchRequestParamVo, Model model) {
        SearchResponseVo searchResponseVo = searchService.search(searchRequestParamVo);
        model.addAttribute("response",searchResponseVo);
        model.addAttribute("searchParam",searchRequestParamVo);
        return "search";
    }
}
