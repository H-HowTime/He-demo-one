package com.atguigu.gmall.ums.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "aliyun.sms")
public class SmsProperties {
    private String regionId; //: cn-hangzhou #地域ID
    private String accessKeyId; //: "LTAI4GEiESw7Vcys5W7XaRcT" #阿里云KeyId
    private String accessKeySecret; //: "eHBetznpl9EIeMxw4kFBOLfrqtq7UW" #阿里云KeySecret
    private String templateCode; //: SMS_205881616  #短信模板code
    private String signName; //: 美年旅游 #短信模板签名
}