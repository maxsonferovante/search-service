package com.maal.searchservice.infra.persistence.adapter;


import com.maal.searchservice.domain.modal.FlightPrice;
import com.maal.searchservice.infra.persistence.entity.FlightPriceEntity;
import com.maal.searchservice.infra.persistence.mapper.FlightPriceMapper;
import com.maal.searchservice.infra.persistence.repository.JpaFlightPriceRepository;
import com.maal.searchservice.domain.repository.FlightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaFligthPriceAdapter  implements FlightRepository{


    private final JpaFlightPriceRepository jpaFlightPriceRepository;
    private final FlightPriceMapper flightPriceMapper;


    @Override
    public void save(FlightPrice flightPrice) {
        jpaFlightPriceRepository.save(
                flightPriceMapper.toEntity(flightPrice)
        );
    }

    @Override
    public Optional<FlightPrice> findById(Long flightId) {
        var flightPriceEntity = jpaFlightPriceRepository.findById(flightId);
        return flightPriceEntity.map(flightPriceMapper::toDomain);
    }

    @Override
    public List<FlightPrice> findByPriceBetweenMinAndMax(String origin, String destination, String travelDate, BigDecimal minPrice, BigDecimal maxPrice) {
        List<FlightPriceEntity> flightPriceEntities =  jpaFlightPriceRepository.findByPriceBetween(minPrice, maxPrice);
        return flightPriceEntities.stream()
                .map(flightPriceMapper::toDomain)
                .filter(flightPrice -> flightPrice.getOrigin().equalsIgnoreCase(origin) &&
                        flightPrice.getDestination().equalsIgnoreCase(destination) &&
                        flightPrice.getTravelDate().toString().equalsIgnoreCase(travelDate))
                .toList();
    }
}
