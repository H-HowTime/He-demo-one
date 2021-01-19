package com.atguigu.gmall.ums.mapper;

import com.atguigu.gmall.ums.entity.UserEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表
 * 
 * @author hehao
 * @email hehao@hehao.com
 * @date 2021-01-18 23:36:16
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
	
}
