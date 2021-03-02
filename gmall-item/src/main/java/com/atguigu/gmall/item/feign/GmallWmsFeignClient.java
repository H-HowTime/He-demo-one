package com.atguigu.gmall.item.feign;

import com.atguigu.gmall.wms.api.GmallWmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author hehao
 * @create 2021-01-27 18:52
 */
@FeignClient("gmall-wms")
public interface GmallWmsFeignClient extends GmallWmsApi {
}
