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
        entity.setOutboundDate(domain.getOutboundDate());
        entity.setReturnDate(domain.getReturnDate());
        entity.setTargetPrice(domain.getTargetPrice());
        entity.setToleranceUp(domain.getToleranceUp());
        entity.setCurrency(domain.getCurrency());
        entity.setActive(domain.getActive());
        return entity;
    }

    public WatchRoute toDomain(WatchRouteEntity entity) {
        return new WatchRoute(
                entity.getAlertId(),
                entity.getOrigin(),
                entity.getDestination(),
                entity.getOutboundDate(),
                entity.getReturnDate(),
                entity.getTargetPrice(),
                entity.getToleranceUp(),
                entity.getCurrency(),
                entity.getActive()
        );
    }
}
