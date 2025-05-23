package com.maal.searchservice.infra.persistence.mapper;

import com.maal.searchservice.domain.modal.WatchRoute;
import com.maal.searchservice.infra.persistence.entity.WatchRouteEntity;
import org.springframework.stereotype.Component;

// Mapper utilitário
@Component
public class WatchRouteMapper {

    public WatchRouteEntity toEntity(WatchRoute domain) {
        WatchRouteEntity entity = new WatchRouteEntity();
        entity.setAlertId(domain.getAlertId());
        entity.setOrigin(domain.getOrigin());
        entity.setDestination(domain.getDestination());
        entity.setTravelDate(domain.getTravelDate());
        entity.setTargetPrice(domain.getTargetPrice());
        entity.setToleranceUp(domain.getToleranceUp());
        entity.setCurrency(domain.getCurrency());
        entity.setActive(domain.getActive());
        return entity;
    }

    public WatchRoute toDomain(WatchRouteEntity entity) {
        return new com.maal.searchservice.domain.modal.WatchRoute(
                entity.getAlertId(),
                entity.getOrigin(),
                entity.getDestination(),
                entity.getTravelDate(),
                entity.getTargetPrice(),
                entity.getToleranceUp(),
                entity.getCurrency(),
                entity.getActive()
        );
    }
}
