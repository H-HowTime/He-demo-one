package com.atguigu.gmall.oms.feign;

import com.atguigu.gmall.wms.api.GmallWmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author hehao
 * @create 2021-02-22 18:09
 */
@FeignClient("gmall-wms")
public interface GmallWmsFeignClient extends GmallWmsApi {
}
