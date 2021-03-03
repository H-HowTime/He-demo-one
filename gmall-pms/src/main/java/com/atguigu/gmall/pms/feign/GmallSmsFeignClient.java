package com.atguigu.gmall.pms.feign;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.vo.SkuSaleVo;
import com.atguigu.gmall.sms.api.GmallSmsApi;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author hehao
 * @create 2021-01-20 20:42
 */
@FeignClient("gmall-sms")
public interface GmallSmsFeignClient extends GmallSmsApi {
}
