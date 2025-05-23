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

    private UUID messageId;
    private String route;
    private LocalDate date;
    private BigDecimal oldPrice;
    private Currency currency;
    private Instant checkedAt;
}
