package com.maal.searchservice.infra.persistence.adapter;


import com.maal.searchservice.domain.modal.PriceUpdated;
import com.maal.searchservice.domain.repository.PriceUpdatedRepository;
import com.maal.searchservice.infra.persistence.entity.PriceUpdatedEntity;
import com.maal.searchservice.infra.persistence.mapper.PriceUpdatedMapper;
import com.maal.searchservice.infra.persistence.repository.JpaPriceUpdatedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;


@Repository
@RequiredArgsConstructor
public class JpaPriceUpdatedAdapter implements PriceUpdatedRepository {

    private final JpaPriceUpdatedRepository jpaPriceUpdatedRepository;
    private final PriceUpdatedMapper priceUpdatedMapper;


    @Override
    public void save(PriceUpdated priceUpdated) {
        jpaPriceUpdatedRepository.save(priceUpdatedMapper.toEntity(priceUpdated));
    }
}
