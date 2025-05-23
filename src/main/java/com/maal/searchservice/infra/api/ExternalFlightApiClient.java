package com.maal.searchservice.infra.api;

import com.maal.searchservice.infra.api.dto.FlightApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "external-flight-api",
        url = "${external.flight.api.url}",
        configuration = ExternalFlightApiConfig.class) // Adiciona a configuração do interceptor
public interface ExternalFlightApiClient {

    @GetMapping() // Ou o endpoint correto
    FlightApiResponse getFlightResults(
            @RequestParam("departure_id") String departureId,
            @RequestParam("arrival_id") String arrivalId,
            @RequestParam("outbound_date") String outboundDate,
            @RequestParam("return_date") String returnDate
    );
}