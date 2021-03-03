package com.atguigu.gmall.search.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author hehao
 * @create 2021-01-27 18:52
 */
@FeignClient("gmall-pms")
public interface GmallPmsFeignClient extends GmallPmsApi {
}
