package com.atguigu.gmall.oms.listener;

import com.atguigu.gmall.oms.mapper.OrderMapper;
import com.rabbitmq.client.Channel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author hehao
 * @create 2021-02-28 17:18
 */
@Component
public class OrderListener {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "ORDER_DISABLE_QUEUE", durable = "true"),
            exchange = @Exchange(name = "ORDER_EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"order.disable"}
    ))
    public void orderDisableListener(String orderToken, Message message, Channel channel) throws IOException {
        if (StringUtils.isBlank(orderToken)) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }
        //更新订单状态为失效订单
        Integer rows = this.orderMapper.updateStatus(orderToken, 0, 5);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    @RabbitListener(queues = {"DEAD_CLOSE_ORDER_QUEUE"})
    public void closeOrderListener(String orderToken, Message message, Channel channel) throws IOException {
        if (StringUtils.isBlank(orderToken)) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }
        //完成定时关单
        //更新订单状态为关闭状态
        if (this.orderMapper.updateStatus(orderToken, 0, 4) == 1) {
            //关闭订单成功，发送消息给wms，解锁库存
            this.rabbitTemplate.convertAndSend("ORDER_EXCHANGE", "order.disable", orderToken);
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "ORDER_PAY_SUCCESS_QUEUE", durable = "true"),
            exchange = @Exchange(name = "ORDER_EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"pay.success"}
    ))
    public void orderPaySuccessListener(String orderToken, Message message, Channel channel) throws IOException {
        if (StringUtils.isBlank(orderToken)) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }
        //更新订单状态为代发货订单
        if (this.orderMapper.updateStatus(orderToken, 0, 1) == 1) {
            //更新订单状态成功，发送消息给wms,减少商品库存
            this.rabbitTemplate.convertAndSend("ORDER_EXCHANGE", "stock.minus", orderToken);
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

}
