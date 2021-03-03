package com.atguigu.gmall.oms.feign;

import com.atguigu.gmall.sms.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author hehao
 * @create 2021-02-22 18:09
 */
@FeignClient("gmall-sms")
public interface GmallSmsFeignClient extends GmallSmsApi {
}
