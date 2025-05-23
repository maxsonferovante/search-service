package com.maal.searchservice.infra.persistence.mapper;


import com.maal.searchservice.domain.modal.FlightPrice;
import com.maal.searchservice.infra.persistence.entity.FlightPriceEntity;
import org.springframework.stereotype.Component;

@Component
public class FlightPriceMapper {

    public FlightPriceEntity toEntity(FlightPrice domain) {
        FlightPriceEntity entity = new FlightPriceEntity();
        entity.setOrigin(domain.getOrigin());
        entity.setDestination(domain.getDestination());
        entity.setTravelDate(domain.getTravelDate());
        entity.setPrice(domain.getPrice());
        entity.setCurrency(domain.getCurrency());
        entity.setCheckedAt(domain.getCheckedAt());
        return entity;
    }

    public FlightPrice toDomain(FlightPriceEntity entity) {
        return new FlightPrice(
                entity.getOrigin(),
                entity.getDestination(),
                entity.getTravelDate(),
                entity.getPrice(),
                entity.getCurrency(),
                entity.getCheckedAt()
        );
    }
}
