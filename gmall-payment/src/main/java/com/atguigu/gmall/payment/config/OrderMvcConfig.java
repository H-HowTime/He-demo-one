package com.atguigu.gmall.payment.config;


import com.atguigu.gmall.payment.intercept.LoginInterceptHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author hehao
 * @create 2021-02-23 18:11
 */
@Configuration
public class OrderMvcConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptHandler loginInterceptHandler;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptHandler).addPathPatterns("/**").excludePathPatterns("/pay/**");//配置拦截器的拦截路径
    }
}
