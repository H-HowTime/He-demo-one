package com.atguigu.gmall.common.exception;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @author hehao
 * @create 2021-02-22 11:24
 */
@Slf4j
@RestControllerAdvice
public class GmallExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseVo<String> exception(Exception e) {
        log.error("异常为:{}" + e.getMessage());
        e.printStackTrace();
        return ResponseVo.ok(e.getMessage());
    }
}
