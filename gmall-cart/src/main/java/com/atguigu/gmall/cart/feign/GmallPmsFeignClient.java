package com.atguigu.gmall.cart.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author hehao
 * @create 2021-02-23 17:20
 */
@FeignClient("gmall-pms")
public interface GmallPmsFeignClient extends GmallPmsApi {
}
