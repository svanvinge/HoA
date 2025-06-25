package com.odevo.hoa.worker.config;

import com.odevo.hoa.common.util.Constants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for RabbitMQ in the worker.
 * Defines the exchange, queue, and binding to consume messages.
 */
@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(Constants.RABBITMQ_EXCHANGE_NAME);
    }

    @Bean
    public Queue queue() {
        return new Queue(Constants.RABBITMQ_QUEUE_NAME, true); // durable queue
    }

    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue)
                .to(exchange)
                .with(Constants.RABBITMQ_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        // Use Jackson2JsonMessageConverter for automatic JSON serialization/deserialization
        return new Jackson2JsonMessageConverter();
    }
}
