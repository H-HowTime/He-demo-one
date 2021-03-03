package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.sms.api.GmallSmsApi;
import com.atguigu.gmall.ums.api.GmallUmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author hehao
 * @create 2021-02-22 18:09
 */
@FeignClient("gmall-sms")
public interface GmallSmsFeignClient extends GmallSmsApi {
}
