package com.atguigu.gmall.payment.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.exception.GmallException;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.payment.intercept.LoginInterceptHandler;
import com.atguigu.gmall.payment.pojo.UserInfo;
import com.atguigu.gmall.payment.service.PaymentService;
import com.atguigu.gmall.payment.vo.PayAsyncVo;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hehao
 * @create 2021-03-01 17:50
 */
@Controller
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 跳转到支付选项页面
     *
     * @return
     */
    @GetMapping("pay.html")
    public String toPay(@RequestParam("orderToken") String orderToken, Model model) {
        OrderEntity orderEntity = this.paymentService.toPay(orderToken);
        model.addAttribute("orderEntity", orderEntity);
        return "pay";
    }

    /**
     * 通过的支付宝的接口跳转到支付宝支付页面
     */
    @GetMapping("alipay.html")
    @ResponseBody
    public String alipay(@RequestParam("orderToken") String orderToken) {
        String form = this.paymentService.alipay(orderToken);
        return form;
    }

    /**
     * 支付宝支付成功同步回调
     */
    @GetMapping("pay/success")
    public String paySuccess() {
        return "paysuccess";
    }

    /**
     * 支付宝支付成功异步回调
     */
    @PostMapping("pay/ok")
    @ResponseBody
    public String payOk(PayAsyncVo payAsyncVo) {
        String response = this.paymentService.payOk(payAsyncVo);
        return response;
    }

    /**
     * 秒杀案例
     *
     * @param skuId
     * @return
     */
    @GetMapping("seckill/{skuId}")
    public ResponseVo<Object> seckill(@PathVariable("skuId") Long skuId) {

        RLock fairLock = this.redissonClient.getFairLock("seckill:lock:" + skuId);
        fairLock.lock();

        // 判断库存是否充足
        String stockString = this.stringRedisTemplate.opsForValue().get("seckill:stock:" + skuId);
        if (StringUtils.isBlank(stockString) || Integer.parseInt(stockString) == 0) {
            throw new GmallException("秒杀不存在或者秒杀已结束！");
        }

        // 减库存
        this.stringRedisTemplate.opsForValue().decrement("seckill:stock:" + skuId);

        // 发送消息异步创建订单，并减库存
        Map<String, Object> msg = new HashMap<>();
        msg.put("skuId", skuId);
        msg.put("count", 1);
        UserInfo userInfo = LoginInterceptHandler.getUserInfo();
        msg.put("userId", userInfo.getUserId());
        String orderToken = IdWorker.getTimeId();
        msg.put("orderToken", orderToken);
        this.rabbitTemplate.convertAndSend("ORDER_EXCHANGE", "seckill.success", msg);

        RCountDownLatch countDownLatch = this.redissonClient.getCountDownLatch("seckill:countdown:" + orderToken);
        countDownLatch.trySetCount(1);

        fairLock.unlock();
        return ResponseVo.ok("秒杀成功！" + orderToken);
    }

    @GetMapping("order/{orderToken}")
    public ResponseVo<OrderEntity> queryOrderByToken(@PathVariable("orderToken") String orderToken) throws InterruptedException {

        RCountDownLatch countDownLatch = this.redissonClient.getCountDownLatch("seckill:countdown:" + orderToken);
        countDownLatch.await();

        UserInfo userInfo = LoginInterceptHandler.getUserInfo();
        OrderEntity orderEntity = this.paymentService.queryOrderByToken(orderToken);
        if (orderEntity.getUserId() == userInfo.getUserId()) {
            return ResponseVo.ok(orderEntity);
        }
        return ResponseVo.ok();
    }
}
