package com.maal.searchservice.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maal.searchservice.domain.modal.WatchRoute;
import com.maal.searchservice.domain.repository.WatchRouteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class HandleAlertCreatedService {

    private final WatchRouteRepository repo;

    public HandleAlertCreatedService(WatchRouteRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public void handle(WatchRoute route) {
        if (repo.findByAlertId(route.getAlertId()).isPresent()) {
            log.info("Alert {} já existe, ignorando.", route.getAlertId());
            return;
        }
        route.setActive(true);
        repo.upsert(route);                       // idempotência pelo CONSTRAINT no DB
        log.info("Alert {} salvo/ativado.", route.getAlertId());
    }
}
