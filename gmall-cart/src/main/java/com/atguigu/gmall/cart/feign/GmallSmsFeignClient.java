package com.atguigu.gmall.cart.feign;

import com.atguigu.gmall.sms.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author hehao
 * @create 2021-02-23 17:20
 */
@FeignClient("gmall-sms")
public interface GmallSmsFeignClient extends GmallSmsApi {
}
