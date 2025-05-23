package com.maal.searchservice.infra.persistence.repository;
import com.maal.searchservice.infra.persistence.entity.WatchRouteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;


public interface JpaWatchRouteRepository extends JpaRepository<WatchRouteEntity, Long> {
        Collection<WatchRouteEntity> findByActiveTrue();
        Optional<WatchRouteEntity> findByAlertId(Long alertId);
        void deleteByAlertId(Long alertId);
}
