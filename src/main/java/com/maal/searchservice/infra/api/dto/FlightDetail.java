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
public class FlightDetail {
    private AirportInfo departureAirport;
    private AirportInfo arrivalAirport;
    private Integer duration;
    private String airplane;
    private String airline;
    private String airlineLogo;
    private String travelClass;
    private String flightNumber;
    private List<String> extensions; // Assumindo que extensions é uma lista de Strings
    private List<String> ticketAlsoSoldBy; // Assumindo que é uma lista de Strings
    private String legroom;
    private Boolean overnight;
    private Boolean oftenDelayedByOver30Min;
    private String planeAndCrewBy;
}