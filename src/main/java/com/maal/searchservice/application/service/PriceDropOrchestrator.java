package com.maal.searchservice.application.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.maal.searchservice.application.mapper.PriceUpdatedMapper;
import com.maal.searchservice.domain.event.AlertEventPayload;
import com.maal.searchservice.domain.modal.WatchRoute;
import com.maal.searchservice.domain.port.PriceAlertPublisher;
import com.maal.searchservice.domain.repository.PriceHistoryRepository;
import com.maal.searchservice.infra.api.dto.FlightApiResponse;
import org.springframework.stereotype.Service;

@Service
public class PriceDropOrchestrator {

    // This orchestrator will coordinate the price drop detection and alert publishing
    // It will use the PriceChangeDetector to detect significant price drops
    // and then publish alerts using the PriceAlertPublisher.

    // Add necessary dependencies, such as PriceChangeDetector and PriceAlertPublisher

    // Implement methods to orchestrate the flow of detecting price drops and publishing alerts
    public PriceDropOrchestrator(PriceChangeDetector priceChangeDetector,
                                 PriceHistoryRepository previousPriceHistoryRepository,
                                 PriceAlertPublisher priceAlertPublisher,
                                 PriceUpdatedMapper priceUpdatedMapper) {
        this.priceChangeDetector = priceChangeDetector;
        this.previousPriceHistoryRepository = previousPriceHistoryRepository;
        this.priceAlertPublisher = priceAlertPublisher;
        this.priceUpdatedMapper = priceUpdatedMapper;
    }

    private final PriceChangeDetector priceChangeDetector;
    private final PriceHistoryRepository previousPriceHistoryRepository;
    private final PriceAlertPublisher priceAlertPublisher;
    private final PriceUpdatedMapper priceUpdatedMapper;

    public void handleRoute(WatchRoute route, FlightApiResponse apiResp) throws JsonProcessingException {
        priceChangeDetector.detect(route, apiResp).ifPresent(event -> {
            previousPriceHistoryRepository.save(event);
            AlertEventPayload payload = priceUpdatedMapper.toAlertPayload(event);
            priceAlertPublisher.publishPriceAlert(payload);
        });
    }
}
