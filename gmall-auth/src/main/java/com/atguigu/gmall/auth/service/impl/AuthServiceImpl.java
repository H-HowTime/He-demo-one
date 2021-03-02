package com.atguigu.gmall.auth.service.impl;

import com.atguigu.gmall.auth.config.JwtProperties;
import com.atguigu.gmall.auth.feign.GmallUmsFeignClient;
import com.atguigu.gmall.auth.service.AuthService;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.exception.GmallException;
import com.atguigu.gmall.common.utils.CookieUtils;
import com.atguigu.gmall.common.utils.IpUtils;
import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.ums.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hehao
 * @create 2021-02-22 17:32
 */
@EnableConfigurationProperties(JwtProperties.class)
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private GmallUmsFeignClient gmallUmsFeignClient;

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public void login(String loginName, String password, HttpServletRequest request, HttpServletResponse response) {
        //1、校验用户名和密码是否正确，调用远程接口
        ResponseVo<UserEntity> userEntityResponseVo = this.gmallUmsFeignClient.queryUser(loginName, password);
        UserEntity userEntity = userEntityResponseVo.getData();
        System.out.println(userEntity);
        //2、判断用户信息是否为null
        if (userEntity == null) {
            throw new GmallException("用户不存在");
        }
        //3、设置payload载荷
        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("userId", userEntity.getId());
        payloadMap.put("userName", userEntity.getUsername());
        payloadMap.put("ip", IpUtils.getIpAddressAtService(request)); //设置请求的IP地址
        //4、生成jwt
        String token = null;
        try {
            token = JwtUtils.generateToken(payloadMap, jwtProperties.getPrivateKey(), jwtProperties.getExpire());
        } catch (Exception e) {
            throw new GmallException("生成token异常");
        }
        //5、保存jwt到cookie中
        CookieUtils.setCookie(request, response, jwtProperties.getCookieName(), token, jwtProperties.getExpire() * 60);
        //6、保存unick到cookie中
        CookieUtils.setCookie(request, response, jwtProperties.getUnick(), userEntity.getNickname(), jwtProperties.getExpire() * 60);
    }
}
