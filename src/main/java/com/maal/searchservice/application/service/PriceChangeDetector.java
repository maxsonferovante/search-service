package com.maal.searchservice.application.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maal.searchservice.config.RabbitMQConfig;
import com.maal.searchservice.domain.event.AlertEventPayload;
import com.maal.searchservice.domain.modal.WatchRoute;
import com.maal.searchservice.domain.politics.PriceVariationPolicy;
import com.maal.searchservice.domain.port.PriceAlertPublisher;
import com.maal.searchservice.domain.repository.FlightRepository;
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
@RequiredArgsConstructor
@Slf4j
public class PriceChangeDetector {

    /**
     * This class is responsible for detecting price changes in flights.
     * It uses the FlightRepository to fetch flight data and the PriceVariationPolicy to determine if a price change is significant.
     */

    private final FlightRepository flightRepository;
    private final PriceVariationPolicy priceVariationPolicy;
    private final PriceAlertPublisher priceAlertPublisher;

    public void checkForPriceChangesAndNotify(WatchRoute route, FlightApiResponse newFlightData) throws JsonProcessingException {
        if (newFlightData == null) {
            log.error("Não foram recebidos dados de voo para a rota: " + route.getAlertId());
            return;
        }

        // Lógica para encontrar o voo mais relevante/barato na resposta
        Optional<FlightOption> cheapestOption = findCheapestFlight(newFlightData);

        if (cheapestOption.isPresent()) {
            Integer currentPrice = cheapestOption.get().getPrice();
            log.info("Rota: " + route.getOrigin() + "->" + route.getDestination() +
                    ", Preço atual mais baixo: " + currentPrice);
            // Verifica se o preço atual é significativamente diferente do último preço conhecido
            Boolean isSignificantDrop = priceVariationPolicy.isSignificantDrop(
                    route.getTargetPrice(),
                    BigDecimal.valueOf(currentPrice),
                    route.getToleranceUp()
            );
            if (Boolean.TRUE.equals(isSignificantDrop)) {
                log.info("ALERTA DE PREÇO! Para o alerta: " + route.getAlertId() + "Rota: " + route.getOrigin() + "->" + route.getDestination() +
                        ". Preço antigo: " + route.getTargetPrice() + ", Preço novo: " + currentPrice);


                log.info("Enviando alerta para a fila RabbitMQ: " + route.getAlertId());
                AlertEventPayload payload = AlertEventPayload.builder()
                        .messageId(UUID.randomUUID())
                        .origin(route.getOrigin())
                        .destination(route.getDestination())
                        .outboundDate(route.getOutboundDate())
                        .returnDate(route.getReturnDate())
                        .newPrice(BigDecimal.valueOf(currentPrice))
                        .oldPrice(route.getTargetPrice())
                        .currency(route.getCurrency())
                        .checkedAt(Instant.now())
                        .build();

                priceAlertPublisher.publishPriceAlert(payload);
                log.info("Alerta enviado com sucesso para a fila RabbitMQ: " + route.getAlertId());
            } else {
                log.info("Nenhuma alteração significativa de preço detectada para a rota: " + route.getOrigin() + "->" + route.getDestination());
            }

        } else {
            log.info("Nenhuma opção de voo encontrada para a rota: " + route.getOrigin() + "->" + route.getDestination());
        }
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
