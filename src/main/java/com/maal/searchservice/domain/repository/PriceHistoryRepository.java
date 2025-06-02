package com.maal.searchservice.domain.repository;

import com.maal.searchservice.domain.modal.PriceUpdated;

public interface PriceHistoryRepository {
    void save(PriceUpdated priceUpdated);
}
