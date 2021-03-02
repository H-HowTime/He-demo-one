package com.atguigu.gmall.pms;

import com.atguigu.gmall.pms.controller.SpuController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author hehao
 * @create 2021-02-19 18:03
 */
@SpringBootTest
class SpuControllerTest {

    @Autowired
    private SpuController spuController;

    @Test
    void querySpuBySkuId() {
        System.out.println(spuController.querySpuBySkuId(1L));
    }
}