package com.maal.searchservice.application.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maal.searchservice.config.RabbitMQConfig;
import com.maal.searchservice.domain.event.AlertEventPayload;
import com.maal.searchservice.domain.modal.PriceUpdated;
import com.maal.searchservice.domain.modal.WatchRoute;
import com.maal.searchservice.domain.politics.PriceVariationPolicy;
import com.maal.searchservice.domain.port.PriceAlertPublisher;
import com.maal.searchservice.domain.repository.FlightRepository;
import com.maal.searchservice.domain.repository.PriceHistoryRepository;
import com.maal.searchservice.infra.api.dto.FlightApiResponse;
import com.maal.searchservice.infra.api.dto.FlightOption;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class PriceChangeDetector {

    /**
     * This class is responsible for detecting price changes in flights.
     * It uses the FlightRepository to fetch flight data and the PriceVariationPolicy to determine if a price change is significant.
     */

    private final PriceVariationPolicy priceVariationPolicy;

    public PriceChangeDetector(PriceVariationPolicy priceVariationPolicy) {
        this.priceVariationPolicy = priceVariationPolicy;
    }

    public Optional<PriceUpdated> detect(WatchRoute route, FlightApiResponse newFlightData) throws JsonProcessingException {

        // Lógica para encontrar o voo mais relevante/barato na resposta
        Optional<FlightOption> cheapestOption = findCheapestFlight(newFlightData);
        if (cheapestOption.isEmpty()) {
            log.info("Nenhum voo encontrado para a rota: " + route.getOrigin() + "->" + route.getDestination());
            return Optional.empty();
        }
        Integer currentPrice = cheapestOption.get().getPrice();
        BigDecimal newPrice = BigDecimal.valueOf(currentPrice);
        log.info("Rota: " + route.getOrigin() + "->" + route.getDestination() +
                ", Preço atual mais baixo: " + currentPrice);
        // Verifica se o preço atual é significativamente diferente do último preço conhecido
        Boolean isSignificantDrop = priceVariationPolicy.isSignificantDrop(
                route.getTargetPrice(),
                newPrice,
                route.getToleranceUp()
        );

        if (isSignificantDrop.equals(Boolean.FALSE)) {
            log.info("Nenhuma alteração significativa de preço detectada para a rota: " + route.getOrigin() + "->" + route.getDestination());
            return Optional.empty();
        }

        log.info("ALERTA DE PREÇO! Para o alerta: " + route.getAlertId() + "Rota: " + route.getOrigin() + "->" + route.getDestination() +
                ". Preço antigo: " + route.getTargetPrice() + ", Preço novo: " + currentPrice);

        PriceUpdated evt = new PriceUpdated(
            UUID.randomUUID(),
            route.getAlertId(),
            route.getOrigin(),
            route.getDestination(),
            route.getOutboundDate(),
            route.getReturnDate(),
            route.getTargetPrice(),
            newPrice,
            route.getCurrency(),
            route.getTargetPrice(),
            route.getToleranceUp(),
            Instant.now()
        );
        return Optional.of(evt);

    }

    private Optional<FlightOption> findCheapestFlight(FlightApiResponse flightData) {
        ArrayList<FlightOption> allFlights = new ArrayList<>();
        if (flightData.getBestFlights() != null) {
            allFlights.addAll(flightData.getBestFlights());
        }
        if (flightData.getOtherFlights() != null) {
            allFlights.addAll(flightData.getOtherFlights());
        }

        return allFlights.stream()
                .filter(fo -> fo.getPrice() != null)
                .min(Comparator.comparing(FlightOption::getPrice));
    }

}
