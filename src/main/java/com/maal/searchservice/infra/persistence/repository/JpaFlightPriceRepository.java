package com.maal.searchservice.infra.persistence.repository;

import com.maal.searchservice.domain.modal.FlightPrice;
import com.maal.searchservice.infra.persistence.entity.FlightPriceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface JpaFlightPriceRepository extends JpaRepository<FlightPriceEntity, Long> {
    List<FlightPriceEntity> findByPriceBetweenPrices(double minPrice, double maxPrice);
}
