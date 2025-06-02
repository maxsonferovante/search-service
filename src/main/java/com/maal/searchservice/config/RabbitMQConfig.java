package com.maal.searchservice.config;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String FLIGHT_ALERTS_EXCHANGE_NAME = "flight_alerts_exchange";
    public static final String EMAIL_NOTIFICATION_QUEUE_NAME = "email_notification_queue";


    @Bean
    FanoutExchange flightAlertsExchange() {
        return new FanoutExchange(FLIGHT_ALERTS_EXCHANGE_NAME);
    }

    @Bean
    Queue emailNotificationExchange() {
        return new Queue(EMAIL_NOTIFICATION_QUEUE_NAME, true);
    }

    @Bean
    Binding emailBinding(Queue emailNotificationQueue, FanoutExchange flightAlertsExchange) {
        return BindingBuilder.bind(emailNotificationQueue).to(flightAlertsExchange);
    }

}
