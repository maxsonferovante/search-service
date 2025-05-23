package com.maal.searchservice.infra.persistence.mapper;


import com.maal.searchservice.domain.modal.PriceUpdated;
import com.maal.searchservice.infra.persistence.entity.PriceUpdatedEntity;
import org.springframework.stereotype.Component;

@Component
public class PriceUpdatedMapper {



    public PriceUpdatedEntity toEntity(PriceUpdated priceUpdated) {
        PriceUpdatedEntity priceUpdatedEntity = new PriceUpdatedEntity();
        priceUpdatedEntity.setMessageId(priceUpdated.getMessageId());
        priceUpdatedEntity.setRoute(priceUpdated.getRoute());
        priceUpdatedEntity.setDate(priceUpdated.getDate());
        priceUpdatedEntity.setOldPrice(priceUpdated.getOldPrice());
        priceUpdatedEntity.setCurrency(priceUpdated.getCurrency());
        priceUpdatedEntity.setCheckedAt(priceUpdated.getCheckedAt());
        return priceUpdatedEntity;
    }

    public PriceUpdatedEntity toDomain(PriceUpdatedEntity priceUpdatedEntity) {
        return new PriceUpdatedEntity(
                priceUpdatedEntity.getId(),
                priceUpdatedEntity.getMessageId(),
                priceUpdatedEntity.getRoute(),
                priceUpdatedEntity.getDate(),
                priceUpdatedEntity.getOldPrice(),
                priceUpdatedEntity.getCurrency(),
                priceUpdatedEntity.getCheckedAt()
        );
    }
}
