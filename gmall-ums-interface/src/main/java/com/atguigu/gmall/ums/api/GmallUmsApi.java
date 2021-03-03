package com.atguigu.gmall.ums.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.ums.entity.UserAddressEntity;
import com.atguigu.gmall.ums.entity.UserEntity;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author hehao
 * @create 2021-02-22 12:56
 */
public interface GmallUmsApi {

    /**
     * 查询用户
     */
    @GetMapping("ums/user/query")
    @ApiOperation("查询用户")
    public ResponseVo<UserEntity> queryUser(@RequestParam("loginName") String loginName,
                                            @RequestParam("password") String password);

    /**
     * 根据userId 获取地址信息列表
     */
    @GetMapping("ums/useraddress/user/{userId}")
    @ApiOperation("详情查询")
    public ResponseVo<List<UserAddressEntity>> queryUserAddressByUId(@PathVariable("userId") Long userId);

    /**
     * 信息
     */
    @GetMapping("ums/user/{id}")
    @ApiOperation("详情查询")
    public ResponseVo<UserEntity> queryUserById(@PathVariable("id") Long id);
}
