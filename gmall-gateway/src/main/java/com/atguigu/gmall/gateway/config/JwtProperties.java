package com.atguigu.gmall.gateway.config;

import com.atguigu.gmall.common.exception.GmallException;
import com.atguigu.gmall.common.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * @author hehao
 * @create 2021-02-22 17:21
 */
@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String pubKeyPath;// E:\rsa\rsa.pub #公钥路径
    private String cookieName;//GMALL-TOKEN #token名称

    //获取公钥和私钥对象
    private PublicKey publicKey;

    //初始化公钥和私钥对象
    @PostConstruct
    public void init() {
        try {
            publicKey = RsaUtils.getPublicKey(pubKeyPath);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
