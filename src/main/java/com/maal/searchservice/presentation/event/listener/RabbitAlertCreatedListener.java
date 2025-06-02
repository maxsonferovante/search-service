package com.maal.searchservice.presentation.event.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maal.searchservice.application.service.HandleAlertCreatedService;
import com.maal.searchservice.config.RabbitMQConfig;
import com.maal.searchservice.domain.event.AlertCreatedMessage;
import com.maal.searchservice.application.mapper.AlertCreatedMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
class RabbitAlertCreatedListener {

    private final ObjectMapper objectMapper;
    private final AlertCreatedMapper mapper;
    private final HandleAlertCreatedService service;

    public RabbitAlertCreatedListener(ObjectMapper objectMapper,
                                      AlertCreatedMapper mapper,
                                      HandleAlertCreatedService service) {
        this.objectMapper = objectMapper;
        this.mapper = mapper;
        this.service = service;
    }

    @RabbitListener(queues = RabbitMQConfig.ALERTS_CREATED_QUEUE_NAME)
    public void onMessage(String raw) throws JsonProcessingException {
        AlertCreatedMessage msg = objectMapper.readValue(raw, AlertCreatedMessage.class);
        service.handle(mapper.toDomain(msg));
    }
}