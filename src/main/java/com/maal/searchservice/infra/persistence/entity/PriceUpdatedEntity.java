package com.maal.searchservice.infra.persistence.entity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Getter
@Setter
@Entity
@Table(name = "price_updated")
public class PriceUpdatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID messageId;
    private Long alertId;
    private String origin;
    private String destination;
    private LocalDate outboundDate;
    private LocalDate returnDate;
    private BigDecimal oldPrice;
    private BigDecimal newPrice;
    private Currency currency;
    private BigDecimal targetPrice;
    private BigDecimal toleranceUp;
    private Instant checkedAt;
}
