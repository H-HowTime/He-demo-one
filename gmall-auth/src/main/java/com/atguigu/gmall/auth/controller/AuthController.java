package com.atguigu.gmall.auth.controller;

import com.atguigu.gmall.auth.service.AuthService;
import com.atguigu.gmall.auth.service.impl.AuthServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author hehao
 * @create 2021-02-22 17:31
 */
@Controller
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 跳转登录页面
     */
    @GetMapping("toLogin.html")
    public String toLogin(@RequestParam(value = "returnUrl",defaultValue = "http://www.gmall.com") String returnUrl, Model model) {
        //保存发起请求的页面，如果没有的话跳转到默认页面http://www.gmall.com即首页
        model.addAttribute("returnUrl", returnUrl);
        //跳转到登录页面
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam("loginName") String loginName,
            @RequestParam("password") String password,
            @RequestParam("returnUrl") String returnUrl,
            HttpServletRequest request, HttpServletResponse response) {
        this.authService.login(loginName,password,request,response);
        //登录成功，跳转到请求页面
        return "redirect:" + returnUrl;
    }
}
