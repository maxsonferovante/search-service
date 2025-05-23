package com.maal.searchservice.infra.persistence.repository;

import com.maal.searchservice.infra.persistence.entity.FlightPriceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.math.BigDecimal;
import java.util.List;

public interface JpaFlightPriceRepository extends JpaRepository<FlightPriceEntity, Long> {
    List<FlightPriceEntity> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
}
