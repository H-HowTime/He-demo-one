package com.atguigu.gmall.pms.vo;

import lombok.Data;

import java.util.List;

/**
 * @author hehao
 * @create 2021-02-19 13:53
 */
@Data
public class ItemGroupVo {

    private Long groupId;
    private String groupName;
    private List<GroupAttrVo> attrValues;

}
