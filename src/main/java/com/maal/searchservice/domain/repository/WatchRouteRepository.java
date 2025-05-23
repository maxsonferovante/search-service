package com.maal.searchservice.domain.repository;


import com.maal.searchservice.domain.modal.WatchRoute;

import java.util.List;
import java.util.Optional;

public interface WatchRouteRepository {
    List<WatchRoute> findAllActive();
    void upsert(WatchRoute watchRoute);
    Optional<WatchRoute> findByAlertId(Long alertId);
    Optional<WatchRoute> findById(Long id);
    void deleteById(Long id);
    void deleteByAlertId(Long alertId);
}
