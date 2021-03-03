package com.atguigu.gmall.pms.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.service.OssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author hehao
 * @create 2021-01-19 11:52
 */
@RestController
@RequestMapping("pms/oss")
public class OssController {

    @Autowired
    private OssService ossService;

    @GetMapping("policy")
    public ResponseVo policy() {
        ResponseVo responseVo = ossService.policy();
        return responseVo;
    }
}
