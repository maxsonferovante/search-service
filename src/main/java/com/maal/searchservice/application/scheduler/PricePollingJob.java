package com.maal.searchservice.application.scheduler;

import com.maal.searchservice.application.service.PriceChangeDetector;
import com.maal.searchservice.domain.modal.WatchRoute;
import com.maal.searchservice.domain.repository.FlightRepository;
import com.maal.searchservice.domain.repository.WatchRouteRepository;
import com.maal.searchservice.infra.api.ExternalFlightApiClient;
import com.maal.searchservice.infra.api.dto.FlightApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class PricePollingJob {
    private final WatchRouteRepository watchRouteRepository;
    private final FlightRepository flightRepository;
    private final ExternalFlightApiClient externalFlightApiClient;
    private final PriceChangeDetector priceChangeDetector;
    // Executa a cada 30 minutos.
    // Cron: segundo minuto hora dia-do-mês mês dia-da-semana
    // "0 */30 * * * *" = no segundo 0, a cada 30 minutos, de qualquer hora, qualquer dia do mês, qualquer mês, qualquer dia da semana.
    @Scheduled(cron = "0 */1 * * * *")
    // Alternativas:
    // @Scheduled(fixedRate = 30 * 60 * 1000) // Executa 30 minutos após a última *conclusão* ter iniciado
    // @Scheduled(fixedDelay = 30 * 60 * 1000) // Executa 30 minutos após a última *conclusão* ter terminado
    public void pollFlightPrices() {
        log.info("Iniciando job de polling de preços de voos...");
        List<WatchRoute> activeRoutes = watchRouteRepository.findAllActive();

        if (activeRoutes.isEmpty()) {
            log.info("Nenhuma rota ativa encontrada para monitoramento.");
            return;
        }

        log.info("Encontradas {} rotas ativas para verificar.", activeRoutes.size());

        for (WatchRoute route : activeRoutes) {
            log.debug("Verificando rota de alerta id  {} - {} Para: {}", route.getAlertId(), route.getOrigin(), route.getDestination());
            try {
                FlightApiResponse flightData = externalFlightApiClient.getFlightResults(
                        route.getOrigin(),
                        route.getDestination(),
                        route.getOutboundDate().toString(),
                        route.getReturnDate().toString()
                );

                // Delega a detecção de variação e possível notificação/armazenamento
                priceChangeDetector.checkForPriceChangesAndNotify(route, flightData, watchRouteRepository);

            } catch (Exception e) {
                log.error("Erro ao buscar ou processar voos para a rota ID {}: {} -> {}. Erro: {}",
                        route.getAlertId(), route.getOrigin(), route.getDestination(), e.getMessage(), e);
            }
        }
        log.info("Job de polling de preços de voos concluído.");
    }
}
