package com.maal.searchservice.application.service;


import com.maal.searchservice.domain.modal.WatchRoute;
import com.maal.searchservice.domain.politics.PriceVariationPolicy;
import com.maal.searchservice.domain.repository.FlightRepository;
import com.maal.searchservice.domain.repository.WatchRouteRepository;
import com.maal.searchservice.infra.api.dto.FlightApiResponse;
import com.maal.searchservice.infra.api.dto.FlightOption;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;

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


    public void checkForPriceChangesAndNotify(WatchRoute route, FlightApiResponse newFlightData, WatchRouteRepository watchRouteRepository) {
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
                    BigDecimal.valueOf(currentPrice),
                    route.getTargetPrice(),
                    route.getToleranceUp()
            );
            if (Boolean.TRUE.equals(isSignificantDrop)) {
                log.info("ALERTA DE PREÇO! Rota: " + route.getOrigin() + "->" + route.getDestination() +
                        ". Preço antigo: " + route.getTargetPrice() + ", Preço novo: " + currentPrice);
                // Aqui você implementaria a lógica de notificação (ex: enviar email, SMS, etc.)
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
