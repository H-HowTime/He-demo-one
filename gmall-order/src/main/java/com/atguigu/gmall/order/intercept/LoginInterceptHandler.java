package com.atguigu.gmall.order.intercept;

import com.atguigu.gmall.order.config.JwtProperties;
import com.atguigu.gmall.order.pojo.UserInfo;
import com.atguigu.gmall.common.utils.CookieUtils;
import com.atguigu.gmall.common.utils.JwtUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

/**
 * @author hehao
 * @create 2021-02-23 18:07
 */
@EnableConfigurationProperties(JwtProperties.class)
@Component
public class LoginInterceptHandler implements HandlerInterceptor {

    private static final ThreadLocal<UserInfo> THREAD_LOCAL = new ThreadLocal<>();

//    @Autowired
//    private JwtProperties jwtProperties;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //无论是否登录，都需要放行，主要是获取jwt中的用户信息
        System.out.println("拦截器的前置方法");
        UserInfo userInfo = new UserInfo();

        //获取userId
        String userId = request.getHeader("userId");
        userInfo.setUserId(Long.valueOf(userId));
        THREAD_LOCAL.set(userInfo);
        return true;
    }

    public static UserInfo getUserInfo() {
        return THREAD_LOCAL.get();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //这里必须结束ThreadLocal中的资源，因为使用的是Tomcat的线程池，无法结束线程
        THREAD_LOCAL.remove();
    }
}
