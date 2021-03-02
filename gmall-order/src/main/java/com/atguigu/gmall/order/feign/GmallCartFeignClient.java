package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.cart.entity.api.GmallCartApi;
import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author hehao
 * @create 2021-02-22 18:09
 */
@FeignClient("gmall-cart")
public interface GmallCartFeignClient extends GmallCartApi {
}
