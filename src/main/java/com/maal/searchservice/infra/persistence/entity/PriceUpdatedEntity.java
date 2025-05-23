package com.maal.searchservice.infra.persistence.entity;
import jakarta.persistence.Entity;
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
    private String id;
    private UUID messageId;
    private String route;
    private LocalDate date;
    private BigDecimal oldPrice;
    private Currency currency;
    private Instant checkedAt;
}
