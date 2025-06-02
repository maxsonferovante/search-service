package com.maal.searchservice.infra.messaging.publisher;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.maal.searchservice.config.RabbitMQConfig;
import com.maal.searchservice.domain.event.AlertEventPayload;
import com.maal.searchservice.domain.exception.MessagingException;
import com.maal.searchservice.domain.port.PriceAlertPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitPriceAlertPublisher implements PriceAlertPublisher {

    private final RabbitTemplate rabbit;
    private final ObjectMapper mapper;

    @Override
    public void publishPriceAlert(AlertEventPayload event){
        try{
            rabbit.convertAndSend(RabbitMQConfig.FLIGHT_ALERTS_EXCHANGE_NAME,
                    "",
                    mapper.writeValueAsString(event));
        } catch (Exception e) {
            throw new MessagingException(
                    "Erro ao publicar alerta de preço no RabbitMQ: " + e.getMessage(),
                    e.getCause()
            );
        }

    }
}
