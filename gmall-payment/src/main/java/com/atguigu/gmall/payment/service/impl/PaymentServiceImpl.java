package com.atguigu.gmall.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.exception.GmallException;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.payment.config.AlipayTemplate;
import com.atguigu.gmall.payment.feign.GmallOmsFeignClient;
import com.atguigu.gmall.payment.intercept.LoginInterceptHandler;
import com.atguigu.gmall.payment.mapper.PaymentMapper;
import com.atguigu.gmall.payment.pojo.PaymentInfoEntity;
import com.atguigu.gmall.payment.pojo.UserInfo;
import com.atguigu.gmall.payment.service.PaymentService;
import com.atguigu.gmall.payment.vo.PayAsyncVo;
import com.atguigu.gmall.payment.vo.PayVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author hehao
 * @create 2021-03-01 17:49
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private GmallOmsFeignClient gmallOmsFeignClient;

    @Autowired
    private AlipayTemplate alipayTemplate;

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public OrderEntity queryOrderByToken(String orderToken) {
        ResponseVo<OrderEntity> orderEntityResponseVo = this.gmallOmsFeignClient.queryOrderByOrderToken(orderToken);
        return orderEntityResponseVo.getData();
    }

    @Override
    public OrderEntity toPay(String orderToken) {
        ResponseVo<OrderEntity> orderEntityResponseVo = gmallOmsFeignClient.queryOrderByOrderToken(orderToken);
        OrderEntity orderEntity = orderEntityResponseVo.getData();
        if (orderEntity == null) {
            throw new GmallException("要支付的订单不存在");
        }
        //判断该订单是否属于该用户
        UserInfo userInfo = LoginInterceptHandler.getUserInfo();
        if (orderEntity.getUserId() != userInfo.getUserId()) {
            throw new GmallException("该订单不属于你");
        }
        //判断该订单是否为待支付状态
        if (orderEntity.getStatus() != 0) {
            throw new GmallException("该订单无法支付");
        }
        return orderEntity;
    }

    @Override
    public String alipay(String orderToken) {
        ResponseVo<OrderEntity> orderEntityResponseVo = gmallOmsFeignClient.queryOrderByOrderToken(orderToken);
        OrderEntity orderEntity = orderEntityResponseVo.getData();
        if (orderEntity == null) {
            throw new GmallException("要支付的订单不存在");
        }
        //判断该订单是否属于该用户
        UserInfo userInfo = LoginInterceptHandler.getUserInfo();
        if (orderEntity.getUserId() != userInfo.getUserId()) {
            throw new GmallException("该订单不属于你");
        }
        //判断该订单是否为待支付状态
        if (orderEntity.getStatus() != 0) {
            throw new GmallException("该订单无法支付");
        }
        //调用支付宝接口，获取支付表单
        PayVo payVo = new PayVo();
        payVo.setOut_trade_no(orderToken);
        payVo.setTotal_amount("0.01");
        payVo.setSubject("谷粒商城订单支付平台");
        //生成对账记录
        String pId = this.createPayInfo(orderEntity);
        payVo.setPassback_params(pId);
        try {
            String form = alipayTemplate.pay(payVo);
            System.out.println("哈哈哈" + form);
            return form;
        } catch (AlipayApiException e) {
            e.printStackTrace();
            throw new GmallException("支付出错，请刷新后重试");
        }
    }

    @Override
    public String payOk(PayAsyncVo payAsyncVo) {
        //验签 商户系统接收到异步通知以后，必须通过验签（验证通知中的 sign 参数）来确保支付通知是由支付宝发送的
        Boolean checkSignature = this.alipayTemplate.checkSignature(payAsyncVo);
        if (!checkSignature) {
            return "failure";
        }
        //校验业务参数  接收到异步通知并验签通过后，请务必核对通知中的 app_id、out_trade_no、total_amount 等参数值是否与请求中的一致，并根据 trade_status 进行后续业务处理
        String app_id = payAsyncVo.getApp_id();
        String out_trade_no = payAsyncVo.getOut_trade_no();
        String total_amount = payAsyncVo.getTotal_amount();
        String pId = payAsyncVo.getPassback_params();
        //根据pid查询对账记录
        PaymentInfoEntity paymentInfoEntity = this.queryPaymentInfoByPid(pId);
        if (!StringUtils.equals(app_id, alipayTemplate.getApp_id())
                || !StringUtils.equals(out_trade_no, paymentInfoEntity.getOutTradeNo())
                || new BigDecimal(total_amount).compareTo(paymentInfoEntity.getTotalAmount()) != 0) {
            return "failure";
        }
        //检验支付状态
        String status = payAsyncVo.getTrade_status();
        if (!StringUtils.equals("TRADE_SUCCESS", status)) {
            return "failure";
        }
        //更新支付对账记录中的状态
        if (this.updatePayment(payAsyncVo, pId) == 0) {
            return "failure";
        }
        //发送异步消息，（oms,更新订单状态，wms 减少库存量）
        rabbitTemplate.convertAndSend("ORDER_EXCHANGE", "pay.success", out_trade_no);
        System.out.println("哈哈哈" + payAsyncVo);
        //响应消息给支付宝
        return "success";
    }

    private int updatePayment(PayAsyncVo payAsyncVo, String pId) {
        PaymentInfoEntity paymentInfoEntity = this.paymentMapper.selectById(pId);
        paymentInfoEntity.setTradeNo(payAsyncVo.getTrade_no());
        paymentInfoEntity.setPaymentStatus(1);
        paymentInfoEntity.setCallbackTime(new Date());
        paymentInfoEntity.setCallbackContent(JSON.toJSONString(payAsyncVo));
        return this.paymentMapper.updateById(paymentInfoEntity);
    }

    /**
     * 生成对账记录
     *
     * @param orderEntity
     * @return
     */
    private String createPayInfo(OrderEntity orderEntity) {
        PaymentInfoEntity paymentInfoEntity = new PaymentInfoEntity();
        paymentInfoEntity.setCreateTime(new Date());
        paymentInfoEntity.setPaymentType(orderEntity.getPayType());
        paymentInfoEntity.setSubject("谷粒电商支付平台");
        paymentInfoEntity.setTotalAmount(new BigDecimal("0.01"));
        paymentInfoEntity.setOutTradeNo(orderEntity.getOrderSn());
        paymentInfoEntity.setPaymentStatus(0);
        this.paymentMapper.insert(paymentInfoEntity);
        return paymentInfoEntity.getId().toString();
    }

    /**
     * 查询对账记录
     *
     * @return
     */
    private PaymentInfoEntity queryPaymentInfoByPid(String pid) {
        return this.paymentMapper.selectById(pid);
    }

}
