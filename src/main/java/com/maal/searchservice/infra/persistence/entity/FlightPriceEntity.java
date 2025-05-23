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

/**
 * FlightPriceEntity class represents the price of a flight.
 * It contains the currency and the amount of the flight price.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "flight_price")
public class FlightPriceEntity {

    @Id
    private Long id;

    private String origin;
    private String destination;
    LocalDate travelDate;
    BigDecimal price;
    /**

     * | currency Diz em **qual moeda** o preço está expresso.
     *      Assim o serviço pode comparar valores corretos e, se necessário, converter ou exibir “R\$ 499,99” vs. “USD 220.00”.                                                 | `Currency.getInstance("BRL")`           |
     * | checkedAt Momento exato (timestamp, em UTC) em que aquele preço foi coletado ou em que o evento foi gerado.
     * Útil para saber quão fresca é a cotação, ordenar histórico e evitar comparar preços com datas muito distantes. | Instant.parse("2025-05-15T12:30:05Z")      * */
    Currency currency;
    Instant checkedAt;
}
