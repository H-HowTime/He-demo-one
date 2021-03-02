package com.atguigu.gmall.common.exception;

import lombok.Data;

@Data
public class GmallException extends RuntimeException{
    private Boolean success; //是否成功
    private Integer code; //响应状态码
    private String message; //错误信息

    public GmallException(Boolean success, Integer code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }

    public GmallException( String message) {
        this.message = message;
    }

}