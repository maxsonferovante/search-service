package com.maal.searchservice.application.mapper;

import com.maal.searchservice.domain.modal.WatchRoute;
import com.maal.searchservice.domain.event.AlertCreatedMessage;
import org.springframework.stereotype.Component;

import java.util.Currency;

/**
 * Conversor manual (sem MapStruct) entre a mensagem recebida da fila
 * e o objeto de domínio {@link WatchRoute}.
 */

@Component
public final class AlertCreatedMapper {

    private AlertCreatedMapper() { /* util class */ }

    /**
     * Converte {@link AlertCreatedMessage} em {@link WatchRoute},
     * atribuindo {@code active=true} por padrão.
     */
    public static WatchRoute toDomain(AlertCreatedMessage dto) {
        if (dto == null) return null;

        WatchRoute route = new WatchRoute();
        route.setAlertId(dto.getAlertId());
        route.setOrigin(dto.getOrigin());
        route.setDestination(dto.getDestination());
        // Se teu WatchRoute tiver outbound/return separados, ajusta aqui:
        route.setOutboundDate(dto.getOutboundDate());
        route.setReturnDate(dto.getReturnDate());
        route.setTargetPrice(dto.getTargetPrice());
        route.setToleranceUp(dto.getToleranceUp());
        route.setCurrency(Currency.getInstance(dto.getCurrency()));
        route.setActive(Boolean.TRUE);
        return route;
    }

    /**
     * (Opcional) Converte do domínio para DTO caso precises reenviar a mensagem.
     */
    public static AlertCreatedMessage toDto(WatchRoute domain) {
        if (domain == null) return null;

        return new AlertCreatedMessage(
                domain.getAlertId(),
                domain.getOrigin(),
                domain.getDestination(),
                domain.getOutboundDate(),
                domain.getReturnDate(),
                domain.getTargetPrice(),
                domain.getToleranceUp(),
                domain.getCurrency().getCurrencyCode()
        );
    }
}
