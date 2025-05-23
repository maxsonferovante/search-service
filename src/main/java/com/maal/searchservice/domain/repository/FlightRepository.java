package com.maal.searchservice.domain.repository;

import com.maal.searchservice.domain.modal.FlightPrice;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface FlightRepository {
    void save(FlightPrice flightPrice);
    Optional<FlightPrice> findById(Long flightId);
    List<FlightPrice> findByPriceBetweenMinAndMax(String origin, String destination, String travelDate, BigDecimal minPrice, BigDecimal maxPrice);
}
