package com.atguigu.gmall.auth.config;

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
    private String priKeyPath;//E:\rsa\rsa.pri #私钥路径
    private String secret;//123abc!@# #生成私钥公钥需要的盐
    private Integer expire;// 180 #token过期时间
    private String cookieName;//GMALL-TOKEN #token名称
    private String unick;//unick #登录成功后页面显示的用户信息

    //获取公钥和私钥对象
    private PublicKey publicKey;
    private PrivateKey privateKey;

    //初始化公钥和私钥对象
    @PostConstruct
    public void init() {
        File pubKeyFile = new File(pubKeyPath);
        File priKeyFile = new File(priKeyPath);
        try {
            if (!pubKeyFile.exists() || !priKeyFile.exists()) {
                //公钥或私钥不存在，生成公钥和私钥
                RsaUtils.generateKey(pubKeyPath, priKeyPath, secret);
            }
            publicKey = RsaUtils.getPublicKey(pubKeyPath);
            privateKey = RsaUtils.getPrivateKey(priKeyPath);
        } catch (Exception e) {
            throw new GmallException(e.getMessage());
        }
    }

}
