package com.maal.searchservice.infra.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FlightOption {
    private List<FlightDetail> flights;
    private List<Layover> layovers;
    private Integer totalDuration;
    private CarbonEmissions carbonEmissions;
    private Integer price;
    private String type;
    private String airlineLogo;
    private List<String> extensions; // Assumindo que extensions é uma lista de Strings
    private String departureToken;
    private String bookingToken;
}