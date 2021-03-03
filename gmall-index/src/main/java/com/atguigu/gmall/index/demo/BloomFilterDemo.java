package com.atguigu.gmall.index.demo;

import com.google.common.base.Charsets;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import jdk.nashorn.internal.runtime.regexp.joni.encoding.CharacterType;

/**
 * @author hehao
 * @create 2021-02-05 22:52
 */
public class BloomFilterDemo {
    public static void main(String[] args) {
        //创建一个波龙过滤器 1-指定保存元素的类型 2-保存元素的个数 3-误报率 默认为0.03  默认hash函数为5个
        BloomFilter<CharSequence> bloomFilter = BloomFilter.create(Funnels.stringFunnel(Charsets.UTF_8), 20, 0.3);
        bloomFilter.put("1");
        bloomFilter.put("2");
        bloomFilter.put("3");
        bloomFilter.put("4");
        bloomFilter.put("5");
        bloomFilter.put("6");
        bloomFilter.put("7");
        bloomFilter.put("8");
        bloomFilter.put("9");
        bloomFilter.put("10");

        System.out.println("------------------------");
        System.out.println(bloomFilter.mightContain("1"));
        System.out.println(bloomFilter.mightContain("3"));
        System.out.println(bloomFilter.mightContain("5"));
        System.out.println(bloomFilter.mightContain("10"));
        System.out.println(bloomFilter.mightContain("12"));
        System.out.println(bloomFilter.mightContain("13"));
        System.out.println(bloomFilter.mightContain("15"));
        System.out.println(bloomFilter.mightContain("14"));
        System.out.println(bloomFilter.mightContain("19"));
        System.out.println(bloomFilter.mightContain("20"));
        System.out.println(bloomFilter.mightContain("21"));
        System.out.println(bloomFilter.mightContain("22"));
        System.out.println(bloomFilter.mightContain("23"));
        System.out.println(bloomFilter.mightContain("24"));
    }
}
