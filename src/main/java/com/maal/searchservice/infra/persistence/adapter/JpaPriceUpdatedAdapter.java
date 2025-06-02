package com.maal.searchservice.infra.persistence.adapter;


import com.maal.searchservice.domain.modal.PriceUpdated;
import com.maal.searchservice.domain.repository.PriceHistoryRepository;
import com.maal.searchservice.infra.persistence.mapper.PriceHistoryMapper;
import com.maal.searchservice.infra.persistence.repository.JpaPriceHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;


@Repository
@RequiredArgsConstructor
public class JpaPriceUpdatedAdapter implements PriceHistoryRepository {

    private final JpaPriceHistoryRepository jpaPriceHistoryRepository;
    private final PriceHistoryMapper priceHistoryMapper;


    @Override
    public void save(PriceUpdated priceUpdated) {
        // Convert the PriceUpdated domain model to the entity using the mapper
        jpaPriceHistoryRepository.save(
            priceHistoryMapper.toEntity(priceUpdated)
        );
    }
}
