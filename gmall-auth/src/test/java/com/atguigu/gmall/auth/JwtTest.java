package com.atguigu.gmall.auth;

import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.common.utils.RsaUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class JwtTest {

    // 别忘了创建D:\\project\rsa目录
	private static final String pubKeyPath = "E:\\rsa\\rsa.pub";
    private static final String priKeyPath = "E:\\rsa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");
    }

    @BeforeEach
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("id", "11");
        map.put("username", "liuyan");
        // 生成token
        String token = JwtUtils.generateToken(map, privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjExIiwidXNlcm5hbWUiOiJsaXV5YW4iLCJleHAiOjE2MTM5ODQwOTl9.HSSvzxzQQ5G3zuZ16vEOmoQ05NnrBZr-cdxpIQfit9gDYhle8nu1qjBGoNam42BptQlx0EDBHkt28malSdmRzntWgLI5c-UEJG3021KnKnEmxe0MWY5svfD_l8MTuqTfLdyqyBvZDvaLEJv48oVRLDguU-DSPB1xBfMDUc58oU4w1tyWw4uXpOAi1e1P1MDZrl8OOEJN6Mn-PRQSD86-EPF627bA9l-1_p6F278fF9mhwTPUHt27PZY59WVrrgTopUhNy-XaYULy0Op1UQh-vCbwZ0R-MRnhFv80INspM6XQ11IUczBvKZJCi8sjPsqemQcWezspJ1ErGR8qF6MVrg";

        // 解析token
        Map<String, Object> map = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + map.get("id"));
        System.out.println("userName: " + map.get("username"));
    }
}