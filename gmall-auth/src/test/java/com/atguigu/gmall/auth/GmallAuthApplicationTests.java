package com.atguigu.gmall.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GmallAuthApplicationTests {

    @Test
    void contextLoads() {
        Long h = null;
        String x = "";
        String y = x + h;
        boolean jj = y.equals("jj");
        System.out.println(h.toString());
        System.out.println(y);
    }

}
