package com.atguigu.gmall.search.pojo;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * @author hehao
 * @create 2021-01-27 17:29
 */
@Data
public class SearchAttrValue {

    @Field(name = "attrId",index = true,type = FieldType.Long)
    private Long attrId;
    @Field(name = "attrName",index = true,type = FieldType.Keyword)
    private String attrName;
    @Field(name = "attrValue",index = true,type = FieldType.Keyword)
    private String attrValue;
}
