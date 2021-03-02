package com.atguigu.gmall.pms;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author hehao
 * @create 2021-02-19 17:38
 */
@SpringBootTest
class CategoryServiceImplTest {

    @Autowired
    private CategoryServiceImpl categoryService;

    @Test
    void query123CategoryByCid() {
        List<CategoryEntity> categoryEntities = categoryService.query123CategoryByCid(225L);
        System.out.println(categoryEntities);
    }
}