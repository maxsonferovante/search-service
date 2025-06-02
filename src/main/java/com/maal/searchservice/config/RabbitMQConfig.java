package com.maal.searchservice.config;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    /* Exchange que o Search‑Service publica (fan‑out). */
    public static final String FLIGHT_ALERTS_EXCHANGE_NAME = "flight_alerts_exchange";

    /* Filas produzidas por este serviço */
    public static final String EMAIL_NOTIFICATION_QUEUE_NAME = "email_notification_queue";

    /* Fila gerenciada por OUTRO serviço — apenas consumida aqui */
    public static final String ALERTS_CREATED_QUEUE_NAME     = "alerts_created_queue";

    @Bean
    FanoutExchange flightAlertsExchange() {
        return new FanoutExchange(FLIGHT_ALERTS_EXCHANGE_NAME);
    }

    @Bean
    Queue emailNotificationQueue() {  // nome reflete o que é
        return new Queue(EMAIL_NOTIFICATION_QUEUE_NAME, true);
    }

    @Bean
    Queue alertsCreatedQueue() {
        return new Queue(ALERTS_CREATED_QUEUE_NAME, true);
    }

    @Bean
    Binding emailBinding(
            @Qualifier("emailNotificationQueue") Queue emailNotificationQueue,
            FanoutExchange flightAlertsExchange) {
        return BindingBuilder.bind(emailNotificationQueue).to(flightAlertsExchange);
    }
}
