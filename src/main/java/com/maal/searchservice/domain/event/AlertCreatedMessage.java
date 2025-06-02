package com.maal.searchservice.domain.event;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AlertCreatedMessage {

    Long      alertId;
    String    origin;
    String    destination;
    LocalDate outboundDate;
    LocalDate returnDate;
    BigDecimal targetPrice;
    BigDecimal toleranceUp;
    String    currency;
}
