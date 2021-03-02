package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author hehao
 * @create 2021-01-20 13:17
 */
public class SpuAttrValueVo extends SpuAttrValueEntity {
    //由于前端传递过来的valueSelected是一个字符串数组
    //将valueSelected中的值转化为以逗号分隔的字符串，设置给attrValue

    private List<String> valueSelected;

    public void setValueSelected(List<String> valueSelected) {
        if(CollectionUtils.isEmpty(valueSelected)){
            return;
        }
        this.setAttrValue(StringUtils.join(valueSelected,","));
    }
}
