package com.maal.searchservice.application.scheduler;

import com.maal.searchservice.application.service.PriceChangeDetector;
import com.maal.searchservice.application.service.PriceDropOrchestrator;
import com.maal.searchservice.domain.modal.WatchRoute;
import com.maal.searchservice.domain.repository.FlightRepository;
import com.maal.searchservice.domain.repository.WatchRouteRepository;
import com.maal.searchservice.infra.api.ExternalFlightApiClient;
import com.maal.searchservice.infra.api.dto.FlightApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;


@Service
@Slf4j
public class PricePollingJob {
    private final WatchRouteRepository watchRouteRepository;
    private final ExternalFlightApiClient externalFlightApiClient;
    private final PriceDropOrchestrator priceDropOrchestrator;
    private final ExecutorService virtualThreadTaskExecutor;
    private final Semaphore apiAccessSemaphore;

    public PricePollingJob(WatchRouteRepository watchRouteRepository,
                           ExternalFlightApiClient externalFlightApiClient,
                           PriceDropOrchestrator priceDropOrchestrator,
                           @Qualifier("virtualThreadTaskExecutor") ExecutorService virtualThreadTaskExecutor,
                           @Qualifier("apiAccessSemaphore") Semaphore apiAccessSemaphore) {
        this.watchRouteRepository = watchRouteRepository;
        this.externalFlightApiClient = externalFlightApiClient;
        this.priceDropOrchestrator = priceDropOrchestrator;
        this.virtualThreadTaskExecutor = virtualThreadTaskExecutor;
        this.apiAccessSemaphore = apiAccessSemaphore;
    }

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
        log.info("Encontradas {} rotas ativas. Submetendo para processamento com limite de concorrência...", activeRoutes.size());

        for (WatchRoute route : activeRoutes) {
            // Não bloqueia o loop principal, a aquisição do semáforo ocorre dentro da virtual thread
            virtualThreadTaskExecutor.submit(() -> {
                boolean permitAcquired = false;
                try {
                    log.debug("VT-{}: Tentando adquirir permissão para rota de alerta id {}",
                            Thread.currentThread().threadId(), route.getAlertId());
                    apiAccessSemaphore.acquire(); // Tenta adquirir uma permissão. Bloqueia se nenhuma estiver disponível.
                    permitAcquired = true;
                    log.info("VT-{}: Permissão adquirida. Verificando rota de alerta id {} - {} Para: {}",
                            Thread.currentThread().threadId(), route.getAlertId(), route.getOrigin(), route.getDestination());

                    FlightApiResponse flightData = externalFlightApiClient.getFlightResults(
                            route.getOrigin(),
                            route.getDestination(),
                            route.getOutboundDate().toString(),
                            route.getReturnDate() != null ? route.getReturnDate().toString() : null
                    );

                    priceDropOrchestrator.handleRoute(route, flightData);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restaura o status de interrupção
                    log.error("VT-{}: Tarefa para rota ID {} interrompida enquanto esperava permissão ou durante execução.",
                            Thread.currentThread().threadId(), route.getAlertId(), e);
                } catch (Exception e) {
                    log.error("VT-{}: Erro ao buscar ou processar voos para a rota ID {}: {} -> {}. Erro: {}",
                            Thread.currentThread().threadId(), route.getAlertId(), route.getOrigin(), route.getDestination(), e.getMessage(), e);
                } finally {
                    if (permitAcquired) {
                        apiAccessSemaphore.release(); // Libera a permissão, crucialmente no bloco finally
                        log.debug("VT-{}: Permissão liberada para rota de alerta id {}",
                                Thread.currentThread().threadId(), route.getAlertId());
                    }
                }
            });
        }
        // O log de "concluído" aqui significa que todas as tarefas foram submetidas.
        // Elas serão executadas respeitando o limite do semáforo.
        log.info("Todas as rotas foram submetidas para processamento. A execução ocorrerá com limite de concorrência.");
    }
}
