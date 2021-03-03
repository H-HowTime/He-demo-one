package com.atguigu.gmall.oms.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author hehao
 * @create 2021-02-22 18:09
 */
@FeignClient("gmall-pms")
public interface GmallPmsFeignClient extends GmallPmsApi {
}
