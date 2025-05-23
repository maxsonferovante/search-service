package com.maal.searchservice.domain.politics;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;



@Component
public class PriceVariationPolicy {

    /**
     * This method checks if the price drop is significant based on the given tolerance.
     *
     * @param oldPrice   The old price of the flight.
     * @param newPrice   The new price of the flight.
     * @param tolerance  The tolerance value to determine if the drop is significant.
     * @return          True if the price drop is significant, false otherwise.
     */
    public Boolean isSignificantDrop(BigDecimal oldPrice, BigDecimal newPrice, BigDecimal tolerance) {
        if (oldPrice == null || newPrice == null || tolerance == null) {
            return false;
        }
        BigDecimal priceDifference = oldPrice.subtract(newPrice).abs();
        return priceDifference.compareTo(tolerance) > 0;

    }
}
