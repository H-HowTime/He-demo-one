package com.atguigu.gmall.pms;

import com.atguigu.gmall.sms.controller.SkuBoundsController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author hehao
 * @create 2021-02-19 18:41
 */
@SpringBootTest
class SkuBoundsControllerTest {

    @Autowired
    private SkuBoundsController skuBoundsController;

    @Test
    void promotionBySkuId() {
        System.out.println(skuBoundsController.promotionBySkuId(5L));
    }
}