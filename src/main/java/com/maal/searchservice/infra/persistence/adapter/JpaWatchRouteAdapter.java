package com.maal.searchservice.infra.persistence.adapter;

import com.maal.searchservice.domain.modal.WatchRoute;
import com.maal.searchservice.domain.repository.WatchRouteRepository;
import com.maal.searchservice.infra.persistence.entity.WatchRouteEntity;
import com.maal.searchservice.infra.persistence.mapper.WatchRouteMapper;
import com.maal.searchservice.infra.persistence.repository.JpaWatchRouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;

import java.util.List;
import java.util.Optional;


@Repository
@RequiredArgsConstructor
public class JpaWatchRouteAdapter implements WatchRouteRepository {

    private final JpaWatchRouteRepository jpaWatchRouteRepository;
    private final WatchRouteMapper watchRouteMapper;

    @Override
    public List<WatchRoute> findAllActive() {
        return jpaWatchRouteRepository.findByActiveTrue().stream()
                .map(watchRouteMapper::toDomain)
                .toList();
    }

    @Override
    public void upsert(WatchRoute watchRoute) {
        var watchRouteEntity = watchRouteMapper.toEntity(watchRoute);
        jpaWatchRouteRepository.save(watchRouteEntity);
    }

    @Override
    public Optional<WatchRoute> findByAlertId(Long alertId) {
        var watchRouteEntity = jpaWatchRouteRepository.findByAlertId(alertId);
        return watchRouteEntity.map(watchRouteMapper::toDomain);
    }

    @Override
    public Optional<WatchRoute> findById(Long id) {
        var watchRouteEntity = jpaWatchRouteRepository.findById(id);
        return watchRouteEntity.map(watchRouteMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        jpaWatchRouteRepository.deleteById(id);
    }

    @Override
    public void deleteByAlertId(Long alertId) {
        jpaWatchRouteRepository.deleteByAlertId(alertId);
    }
}
