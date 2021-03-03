package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.oms.api.GmallOmsApi;
import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author hehao
 * @create 2021-02-22 18:09
 */
@FeignClient("gmall-oms")
public interface GmallOmsFeignClient extends GmallOmsApi {
}
