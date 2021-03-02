package com.atguigu.gmall.cart.feign;

import com.atguigu.gmall.wms.api.GmallWmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author hehao
 * @create 2021-02-23 17:20
 */
@FeignClient("gmall-wms")
public interface GmallWmsFeignClient extends GmallWmsApi {
}
