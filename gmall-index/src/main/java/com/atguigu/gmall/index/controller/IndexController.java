package com.atguigu.gmall.index.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.omg.CORBA.PRIVATE_MEMBER;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author hehao
 * @create 2021-02-02 23:16
 */
@Controller
public class IndexController {

    @Autowired
    private IndexService indexService;

    @GetMapping
    public String toIndex(Model model){
        //获取一级分类
        List<CategoryEntity> categoryEntities = indexService.getCategorys();
        model.addAttribute("categories",categoryEntities);
        //TODO 获取广告信息
        return "index";
    }

    /**
     * http://api.gmall.com/index/cates/1
     */
    @GetMapping(value = "/index/cates/{pid}")
    @ResponseBody
    public ResponseVo<List<CategoryEntity>> getLv2CategoriesByPid(@PathVariable(value = "pid") Long pid){
        //根据pid获取二级分类
        List<CategoryEntity> categoryEntities = indexService.getLv2CategoriesByPid(pid);

        return ResponseVo.ok(categoryEntities);
    }

    @GetMapping(value = "/index/test/lock")
    @ResponseBody
    public ResponseVo<List<Object>> testLock(){
         indexService.testLock();
        return ResponseVo.ok();
    }

    @GetMapping(value = "/index/rwLock/read")
    @ResponseBody
    public ResponseVo<String> testReadLock(){
        indexService.testReadLock();
        return ResponseVo.ok("读数据");
    }

    @GetMapping(value = "/index/rwLock/write")
    @ResponseBody
    public ResponseVo<String> testWriteLock(){
        indexService.testWriteLock();
        return ResponseVo.ok("写数据");
    }

    @GetMapping(value = "/index/cdl/latch")
    @ResponseBody
    public ResponseVo<String> testCdl(){
        indexService.testCdl();
        return ResponseVo.ok("班长关门了");
    }

    @GetMapping(value = "/index/cdl/stu")
    @ResponseBody
    public ResponseVo<String> testStu(){
        indexService.testStu();
        return ResponseVo.ok("学生出来了");
    }
}
