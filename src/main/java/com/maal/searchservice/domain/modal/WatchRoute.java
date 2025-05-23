package com.maal.searchservice.domain.modal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class WatchRoute {

    private Long alertId;
    private String origin;
    private String destination;
    private LocalDate outboundDate;
    private LocalDate returnDate;
    private BigDecimal targetPrice;
    private BigDecimal toleranceUp;
    private Currency currency;
    private Boolean active;
}
