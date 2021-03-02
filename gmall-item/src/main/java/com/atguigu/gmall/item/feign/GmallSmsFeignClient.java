package com.atguigu.gmall.item.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import com.atguigu.gmall.sms.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author hehao
 * @create 2021-01-27 18:52
 */
@FeignClient("gmall-sms")
public interface GmallSmsFeignClient extends GmallSmsApi {
}
