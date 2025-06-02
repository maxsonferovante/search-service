package com.maal.searchservice.application.mapper;


import com.maal.searchservice.domain.event.AlertEventPayload;
import com.maal.searchservice.domain.modal.PriceUpdated;
import org.springframework.stereotype.Component;

@Component
public class PriceUpdatedMapper {
    public AlertEventPayload toAlertPayload(PriceUpdated priceUpdated) {
        return AlertEventPayload.builder()
                .origin(priceUpdated.getOrigin())
                .destination(priceUpdated.getDestination())
                .outboundDate(priceUpdated.getOutboundDate())
                .returnDate(priceUpdated.getReturnDate())
                .oldPrice(priceUpdated.getOldPrice())
                .newPrice(priceUpdated.getNewPrice())
                .currency(priceUpdated.getCurrency())
                .checkedAt(priceUpdated.getCheckedAt())
                .build();
    }
}
