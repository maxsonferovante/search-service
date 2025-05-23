package com.maal.searchservice.infra.persistence.repository;

import com.maal.searchservice.infra.persistence.entity.PriceUpdatedEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPriceUpdatedRepository extends JpaRepository<PriceUpdatedEntity, Long> {
}
