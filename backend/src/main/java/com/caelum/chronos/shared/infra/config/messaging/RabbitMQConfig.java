package com.caelum.chronos.shared.infra.config.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String WORK_ORDER_EXCHANGE = "work-order.exchange";
    public static final String WORK_ORDER_COMPLETED_QUEUE = "work-order.completed.queue";
    public static final String WORK_ORDER_COMPLETED_ROUTING_KEY = "work-order.completed";
    
    public static final String WORK_ORDER_DLQ_EXCHANGE = "work-order.dlq.exchange";
    public static final String WORK_ORDER_COMPLETED_DLQ = "work-order.completed.dlq";

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public DirectExchange workOrderExchange() {
        return new DirectExchange(WORK_ORDER_EXCHANGE);
    }

    @Bean
    public Queue workOrderCompletedQueue() {
        return QueueBuilder.durable(WORK_ORDER_COMPLETED_QUEUE)
                .withArgument("x-dead-letter-exchange", WORK_ORDER_DLQ_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", WORK_ORDER_COMPLETED_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding workOrderCompletedBinding(Queue workOrderCompletedQueue, DirectExchange workOrderExchange) {
        return BindingBuilder.bind(workOrderCompletedQueue)
                .to(workOrderExchange)
                .with(WORK_ORDER_COMPLETED_ROUTING_KEY);
    }

    @Bean
    public DirectExchange workOrderDlqExchange() {
        return new DirectExchange(WORK_ORDER_DLQ_EXCHANGE);
    }

    @Bean
    public Queue workOrderCompletedDlq() {
        return QueueBuilder.durable(WORK_ORDER_COMPLETED_DLQ).build();
    }

    @Bean
    public Binding workOrderCompletedDlqBinding(Queue workOrderCompletedDlq, DirectExchange workOrderDlqExchange) {
        return BindingBuilder.bind(workOrderCompletedDlq)
                .to(workOrderDlqExchange)
                .with(WORK_ORDER_COMPLETED_ROUTING_KEY);
    }
}
