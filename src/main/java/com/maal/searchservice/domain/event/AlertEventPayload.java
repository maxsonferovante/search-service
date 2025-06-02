package com.maal.searchservice.domain.event;


import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AlertEventPayload {
    private UUID messageId;
    private String origin;
    private String destination;
    private LocalDate outboundDate;
    private LocalDate returnDate;
    private BigDecimal oldPrice;
    private BigDecimal newPrice;
    private Currency currency;
    private Instant checkedAt;
}
