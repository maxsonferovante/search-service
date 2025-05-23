package com.maal.searchservice.infra.persistence.entity;





import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "watch_route")
public class WatchRouteEntity {
    @Id
    private Long id;
    private Long alertId;
    private String origin;
    private String destination;
    private LocalDate travelDate;
    private BigDecimal targetPrice;
    private BigDecimal toleranceUp;
    private Currency currency;
    private Boolean active;
}
