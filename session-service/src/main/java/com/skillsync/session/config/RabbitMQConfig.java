package com.skillsync.session.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String SESSION_QUEUE       = "session.queue";
    public static final String SESSION_EXCHANGE    = "session.exchange";
    public static final String SESSION_ROUTING_KEY = "session.booked";

    @Bean
    public Queue sessionQueue() {
        return new Queue(SESSION_QUEUE, true);
    }

    @Bean
    public TopicExchange sessionExchange() {
        return new TopicExchange(SESSION_EXCHANGE);
    }

    @Bean
    public Binding sessionBinding(Queue sessionQueue, TopicExchange sessionExchange) {
        return BindingBuilder
                .bind(sessionQueue)
                .to(sessionExchange)
                .with(SESSION_ROUTING_KEY);
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jacksonMessageConverter());
        return template;
    }
}