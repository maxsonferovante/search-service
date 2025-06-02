package com.maal.searchservice.infra.persistence.mapper;


import com.maal.searchservice.domain.modal.PriceUpdated;
import com.maal.searchservice.infra.persistence.entity.PriceUpdatedEntity;
import org.springframework.stereotype.Component;

@Component
public class PriceHistoryMapper {



    public PriceUpdatedEntity toEntity(PriceUpdated priceUpdated) {
        PriceUpdatedEntity priceUpdatedEntity = new PriceUpdatedEntity();


        priceUpdatedEntity.setMessageId(priceUpdated.getMessageId());
        priceUpdatedEntity.setAlertId(priceUpdated.getAlertId());
        priceUpdatedEntity.setOrigin(priceUpdated.getOrigin());
        priceUpdatedEntity.setDestination(priceUpdated.getDestination());
        priceUpdatedEntity.setOutboundDate(priceUpdated.getOutboundDate());
        priceUpdatedEntity.setReturnDate(priceUpdated.getReturnDate());
        priceUpdatedEntity.setOldPrice(priceUpdated.getOldPrice());
        priceUpdatedEntity.setNewPrice(priceUpdated.getNewPrice());
        priceUpdatedEntity.setCurrency(priceUpdated.getCurrency());
        priceUpdatedEntity.setTargetPrice(priceUpdated.getTargetPrice());
        priceUpdatedEntity.setToleranceUp(priceUpdated.getToleranceUp());
        priceUpdatedEntity.setCheckedAt(priceUpdated.getCheckedAt());
        // Ensure the ID is set if it exists in the domain model
        return priceUpdatedEntity;
    }

    public PriceUpdated toDomain(PriceUpdatedEntity priceUpdatedEntity) {
        return new PriceUpdated(
            priceUpdatedEntity.getMessageId(),
            priceUpdatedEntity.getAlertId(),
            priceUpdatedEntity.getOrigin(),
            priceUpdatedEntity.getDestination(),
            priceUpdatedEntity.getOutboundDate(),
            priceUpdatedEntity.getReturnDate(),
            priceUpdatedEntity.getOldPrice(),
            priceUpdatedEntity.getNewPrice(),
            priceUpdatedEntity.getCurrency(),
            priceUpdatedEntity.getTargetPrice(),
            priceUpdatedEntity.getToleranceUp(),
            priceUpdatedEntity.getCheckedAt()
        );
    }
}
