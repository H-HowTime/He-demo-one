package com.atguigu.gmall.index.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author hehao
 * @create 2021-02-02 23:19
 */
@FeignClient("gmall-pms")
public interface GmallPmsFeignClient extends GmallPmsApi {
}
