package com.atguigu.gmall.index.service;

import com.atguigu.gmall.pms.entity.CategoryEntity;

import java.util.List;

/**
 * @author hehao
 * @create 2021-02-02 23:18
 */
public interface IndexService {
    List<CategoryEntity> getCategorys();

    List<CategoryEntity> getLv2CategoriesByPid(Long pid);

    void testLock();

    void testReadLock();

    void testWriteLock();

    void testCdl();

    void testStu();
}
