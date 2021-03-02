package com.atguigu.gmall.order.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

import javax.annotation.PostConstruct;

/**
 * @author hehao
 * @create 2021-02-28 16:20
 */
@Slf4j
@Configuration
public class RabbitConfirmConfig {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void producerConfirm() {
        //设置交换机成功收到消息的回调，无论是否收到，都会执行
        rabbitTemplate.setConfirmCallback((@Nullable CorrelationData correlationData, boolean ack, @Nullable String cause) -> {
            if (!ack) {
                log.error("消息没有成功到达交换机,原因：{}", cause);
            }
        });
        //设置队列成功收到消息的回调，当队列没有收到消息时才会执行
        rabbitTemplate.setReturnCallback((Message message, int replyCode, String replyText, String exchange, String routingKey) -> {
            log.error("消息没有到达队列！ 交换机：{}，消息：{}，routingKey：{}", exchange, new String(message.getBody()), routingKey);
        });
    }

    /**
     * 定义业务交换机 ORDER_EXCHANGE
     */

    /**
     * 定义业务延时队列 ORDER_TTl_QUEUE
     * 参数设置：
     * x-message-ttl: 90000 延时时间
     * x-dead-latter-exchange: ORDER_EXCHANGE 设置死信交换机
     * x-dead-latter-routing-key: close.order.dead 设置死信交换机 routing-key
     */

    @Bean
    public Queue ttlQueue() {
        return QueueBuilder.durable("ORDER_TTL_QUEUE")
                .withArgument("x-message-ttl", 90000)
                .withArgument("x-dead-letter-exchange", "ORDER_EXCHANGE")
                .withArgument("x-dead-letter-routing-key", "dead.close").build();
    }

    /**
     * 绑定延时队列和业务交换机 close.order
     */
    @Bean
    public Binding ttlBinding() {
        return new Binding("ORDER_TTL_QUEUE", Binding.DestinationType.QUEUE,
                "ORDER_EXCHANGE", "close.order", null);
    }
    /**
     * 定义死信交换机 ORDER_EXCHANGE
     */

    /**
     * 定义死信队列 DEAD_CLOSE_ORDER_QUEUE
     */
    @Bean
    public Queue deadQueue() {
        return QueueBuilder.durable("DEAD_CLOSE_ORDER_QUEUE").build();
    }

    /**
     * 绑定死信队列和死信交换机 dead.close
     */
    @Bean
    public Binding deadBinding() {
        return new Binding("DEAD_CLOSE_ORDER_QUEUE", Binding.DestinationType.QUEUE,
                "ORDER_EXCHANGE", "dead.close", null);
    }
}
