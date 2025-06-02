package com.maal.searchservice.domain.port;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.maal.searchservice.domain.event.AlertEventPayload;

public interface PriceAlertPublisher {

    void publishPriceAlert(AlertEventPayload event) throws JsonProcessingException;
}
