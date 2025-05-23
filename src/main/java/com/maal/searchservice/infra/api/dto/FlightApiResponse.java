package com.maal.searchservice.infra.api.dto;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FlightApiResponse {

    @JsonProperty("best_flights") // Necessário se o nome do campo no JSON for diferente do nome da variável Java
    private List<FlightOption> bestFlights;

    @JsonProperty("other_flights")
    private List<FlightOption> otherFlights;

}