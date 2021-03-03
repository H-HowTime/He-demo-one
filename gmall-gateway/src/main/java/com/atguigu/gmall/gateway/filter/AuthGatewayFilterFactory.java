package com.atguigu.gmall.gateway.filter;

import com.atguigu.gmall.common.utils.IpUtils;
import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.gateway.config.JwtProperties;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author hehao
 * @create 2021-02-22 20:29
 */
@EnableConfigurationProperties(JwtProperties.class)
@Component
public class AuthGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthGatewayFilterFactory.KeyValueConfig> {

    @Autowired
    private JwtProperties jwtProperties;

    public AuthGatewayFilterFactory() {
        super(KeyValueConfig.class);
    }

    public List<String> shortcutFieldOrder(){
        return Arrays.asList("paths");
    }

    @Override
    public ShortcutType shortcutType() {
        return ShortcutType.GATHER_LIST;
    }

    @Override
    public GatewayFilter apply(KeyValueConfig config) {
        return new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                System.out.println("这是自定义的局部过滤器");
                //获取请求和响应对象
                ServerHttpRequest request = exchange.getRequest();
                ServerHttpResponse response = exchange.getResponse();
                //1、判断当前请求路径是在拦截名单中，如果不在，直接放行
                String curPath = request.getURI().getPath(); //当前请求路径
                List<String> paths = config.paths; //拦截名单
                if(CollectionUtils.isEmpty(paths)){
                    return chain.filter(exchange);
                }
                if(!paths.stream().anyMatch(path -> StringUtils.startsWith(curPath,path))){
                    return chain.filter(exchange);
                }
                //2、获取token。异步：在header信息中 同步：在cookie中
                String token =null;
                MultiValueMap<String, HttpCookie> cookies = request.getCookies();
                if(!CollectionUtils.isEmpty(cookies) && cookies.containsKey(jwtProperties.getCookieName())){
                    token = cookies.getFirst(jwtProperties.getCookieName()).getValue();
                    if(StringUtils.isBlank(token)){
                        token = request.getHeaders().getFirst(jwtProperties.getCookieName());
                    }
                }
                //3、判断token是否为空，如果token为扣空，重定向到登录页面
                if(StringUtils.isBlank(token)){
                    response.setStatusCode(HttpStatus.SEE_OTHER); //响应状态码303给浏览器，告诉浏览器重定向
                    response.getHeaders().set(HttpHeaders.LOCATION,"http://sso.gmall.com/toLogin.html?returnUrl=" + request.getURI()); //重定向
                    return response.setComplete();//拦截请求
                }
                try {
                    //4、解析jwt，若果解析异常，重定向到登录页面
                    Map<String, Object> infoFromToken = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
                    //5、判断是否是自己的jwt，判断当前请求的IP地址
                    String ip = infoFromToken.get("ip").toString();
                    String cruIp = IpUtils.getIpAddressAtGateway(request);
                    if(!StringUtils.equals(cruIp,ip)){
                        response.setStatusCode(HttpStatus.SEE_OTHER); //响应状态码303给浏览器，告诉浏览器重定向
                        response.getHeaders().set(HttpHeaders.LOCATION,"http://sso.gmall.com/toLogin.html?returnUrl=" + request.getURI()); //重定向
                        return response.setComplete();//拦截请求
                    }
                    //6、把jwt中的用户信息，传递给后续的服务，使用request头设置用户信息
                    request.mutate().header("userId",infoFromToken.get("userId").toString()).header("userName", infoFromToken.get("userName").toString()).build();
                    exchange.mutate().request(request);
                    //6、放行
                    return chain.filter(exchange);
                } catch (Exception e) {
                    e.printStackTrace();
                    response.setStatusCode(HttpStatus.SEE_OTHER); //响应状态码303给浏览器，告诉浏览器重定向
                    response.getHeaders().set(HttpHeaders.LOCATION,"http://sso.gmall.com/toLogin.html?returnUrl=" + request.getURI()); //重定向
                    return response.setComplete();//拦截请求
                }
            }
        };
    }

    @Data
    public static class KeyValueConfig{

        private List<String> paths;
    }
}
