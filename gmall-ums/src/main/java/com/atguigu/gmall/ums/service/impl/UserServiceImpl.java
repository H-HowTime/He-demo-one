package com.atguigu.gmall.ums.service.impl;

import com.alibaba.fastjson.JSON;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.atguigu.gmall.common.exception.GmallException;
import com.atguigu.gmall.common.exception.GmallExceptionHandler;
import com.atguigu.gmall.common.utils.FormUtils;
import com.atguigu.gmall.ums.config.SmsProperties;
import com.netflix.client.ClientException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.rmi.ServerException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.ums.mapper.UserMapper;
import com.atguigu.gmall.ums.entity.UserEntity;
import com.atguigu.gmall.ums.service.UserService;
import org.springframework.util.CollectionUtils;


@Service("userService")
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private SmsProperties smsProperties;

    private static final String CODE_PREFIX = "gmall:ums:code:";

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<UserEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<UserEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public Boolean checkData(String data, Integer type) {
        //检验用户数据是否能用
        QueryWrapper<UserEntity> wrapper = new QueryWrapper<>();
        switch (type) {
            case 1:
                wrapper.eq("username", data);
                break;
            case 2:
                wrapper.eq("phone", data);
                break;
            case 3:
                wrapper.eq("email", data);
                break;
            default:
                return null;
        }
        return baseMapper.selectCount(wrapper) == 0;
    }

    @Override
    public void sendCode(String phone) {
        //TODO 做一个发送到微服务，使用rabbitmq监听该方法，发送短信
        //验证手机号是否正确
        boolean mobile = FormUtils.isMobile(phone);
        if (StringUtils.isBlank(phone) || !mobile) {
            throw new GmallException("手机号不正确");
        }
        //判断redis中是否过期

        String code2redis = stringRedisTemplate.opsForValue().get(CODE_PREFIX + phone);
        if (!StringUtils.isBlank(code2redis)) {
            return;
        }
        //生成验证码
        String code = UUID.randomUUID().toString().substring(0, 4);

        //发送短信验证码
        DefaultProfile profile = DefaultProfile.getProfile(smsProperties.getRegionId(), smsProperties.getAccessKeyId(), smsProperties.getAccessKeySecret());
        IAcsClient client = new DefaultAcsClient(profile);

        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain("dysmsapi.aliyuncs.com");
        request.setSysVersion("2017-05-25");
        request.setSysAction("SendSms");
        request.putQueryParameter("RegionId", smsProperties.getRegionId());
        request.putQueryParameter("PhoneNumbers", phone);
        request.putQueryParameter("SignName", smsProperties.getSignName());
        request.putQueryParameter("TemplateCode", smsProperties.getTemplateCode());
        //传入动态验证码
        Map<String, String> codeMap = new HashMap<>();
        codeMap.put("code", code);
        request.putQueryParameter("TemplateParam", JSON.toJSONString(codeMap));
        try {
            CommonResponse response = client.getCommonResponse(request);
            String data = response.getData();
            Map map = JSON.parseObject(data, Map.class);
            String status = map.get("Code").toString();
            if (!"OK".equals(status)) {
                throw new GmallException("短信发送失败");
            }
            //发送成功，保存到redis中一份
            stringRedisTemplate.opsForValue().set(CODE_PREFIX + phone, code, 3000L, TimeUnit.MINUTES);
            System.out.println(response.getData());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void registerUser(UserEntity userEntity, String code) {
        //校验验证码
        String code2redis = stringRedisTemplate.opsForValue().get(CODE_PREFIX + userEntity.getPhone());
        if (!StringUtils.equals(code2redis, code)) {
            throw new GmallException("验证码不正确");
        }
        //生成盐
        String salt = StringUtils.replace(UUID.randomUUID().toString(), "-", "").substring(0, 4);
        userEntity.setSalt(salt);
        //加密
        String password = userEntity.getPassword();
        userEntity.setPassword(DigestUtils.md5Hex(password + salt));

        userEntity.setCreateTime(new Date());
        userEntity.setLevelId(1L);
        userEntity.setStatus(1);
        userEntity.setIntegration(200);
        userEntity.setGrowth(200);
        userEntity.setNickname(userEntity.getUsername());
        //新增用户
        int insert = baseMapper.insert(userEntity);
        //删除redis中的验证码
//        if(insert > 0){
//            stringRedisTemplate.delete(CODE_PREFIX + userEntity.getPhone());
//        }
    }

    @Override
    public UserEntity queryUser(String loginName, String password) {
        //查询客户登录名 获取盐salt
        QueryWrapper<UserEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("username", loginName)
                .or().eq("phone", loginName)
                .or().eq("email", loginName);
        List<UserEntity> userEntities = baseMapper.selectList(wrapper);
        //判断用户是否存在
        if (CollectionUtils.isEmpty(userEntities)) {
            return null;
        }
        for (UserEntity userEntity : userEntities) {
            String salt = userEntity.getSalt();
            String pwd2md5 = userEntity.getPassword();
            if (StringUtils.equals(pwd2md5, DigestUtils.md5Hex(password + salt))) {
                return userEntity;

            }
        }
        return null;
    }

}