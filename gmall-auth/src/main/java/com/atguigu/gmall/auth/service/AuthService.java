package com.atguigu.gmall.auth.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author hehao
 * @create 2021-02-22 17:31
 */
public interface AuthService {
    void login(String loginName, String password, HttpServletRequest request, HttpServletResponse response);
}
