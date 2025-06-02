package com.maal.searchservice.domain.modal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class PriceUpdated {

    UUID        messageId;
    Long        alertId;        // quem pediu o alerta
    String      origin;
    String      destination;
    LocalDate   outboundDate;     // ou travelDate se for só ida
    LocalDate   returnDate;
    BigDecimal  oldPrice;
    BigDecimal  newPrice;
    Currency    currency;
    BigDecimal  targetPrice;      // pra análise futura
    BigDecimal  toleranceUp;      // porcentagem aplicada
    Instant     checkedAt;
}
