package com.atguigu.gmall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * @author hehao
 * @create 2021-01-19 9:02
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter(){
        //初始化cors配置对象
        CorsConfiguration configuration = new CorsConfiguration();
        //设置允许的域名。*表示所有域名，但不能携带cookie
        configuration.addAllowedOrigin("http://manager.gmall.com");
        configuration.addAllowedOrigin("http://localhost:1000");
        configuration.addAllowedOrigin("http://127.0.0.1:1000");
        configuration.addAllowedOrigin("http://api.gmall.com");
        configuration.addAllowedOrigin("http://www.gmall.com");
        configuration.addAllowedOrigin("http://gmall.com");
        //允许任何请求头
        configuration.addAllowedHeader("*");
        //允许任何请求方式
        configuration.addAllowedMethod("*");
        //允许携带cookie
        configuration.setAllowCredentials(true);
        //添加拦截路径 /**表示拦截所有请求
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**",configuration);
        return new CorsWebFilter(source);
    }
}
