
---

## 1. Camada **domain**

| Classe / Interface                     | Membros                                                                                                                                                                                                                      | Papel                                                    |
| -------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------- |
| **WatchRoute**                         | • `alertId: Long`  <br>• `origin: String`  <br>• `destination: String`  <br>• `travelDate: LocalDate`  <br>• `targetPrice: BigDecimal`  <br>• `toleranceUp: BigDecimal`  <br>• `currency: Currency`  <br>• `active: boolean` | Representa a rota e a condição que estão sendo vigiadas. |
| **FlightPrice**                        | • `origin, destination, travelDate`  <br>• `price: BigDecimal`  <br>• `currency: Currency`  <br>• `checkedAt: Instant`                                                                                                       | Snapshot do preço coletado.                              |
| **PriceUpdated** *(evento de domínio)* | • `messageId: UUID`  <br>• `route: String`  <br>• `date: LocalDate`  <br>• `oldPrice, newPrice: BigDecimal`  <br>• `currency: Currency`  <br>• `checkedAt: Instant`                                                          | Evento publicado quando o preço cai.                     |
| **PriceVariationPolicy**               | `boolean isSignificantDrop(oldPrice, newPrice, tolerance)`                                                                                                                                                                   | Regra de negócio que decide se a queda é relevante.      |
| **WatchRouteRepository** *(porta)*     | `List<WatchRoute> findAllActive()`  <br>`void upsert(WatchRoute)`                                                                                                                                                            | CRUD da rota vigiada.                                    |
| **FlightPriceRepository** *(porta)*    | `void save(FlightPrice)`  <br>`Optional<FlightPrice> findLatest(origin,dest,date)`                                                                                                                                           | Persistência/histórico de preços.                        |

---

## 2. Camada **application**

| Classe                             | Dependências (→)                                                                             | Responsabilidade                                                                                          |
| ---------------------------------- | -------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------- |
| **PricePollingJob** *(@Scheduled)* | → `WatchRouteRepository`  <br>→ `ExternalFlightApiClient`  <br>→ `PriceChangeDetector`       | Executa de 30 em 30 min: pega todas as rotas ativas, chama a API externa e delega a detecção de variação. |
| **PriceChangeDetector**            | → `FlightPriceRepository`  <br>→ `PriceVariationPolicy`  <br>→ `RabbitPriceUpdatedPublisher` | Salva o preço novo, aplica a política de variação e, se for queda significativa, publica `PriceUpdated`.  |
| **ManualTriggerHandler**           | → `ExternalFlightApiClient`  <br>→ `PriceChangeDetector`                                     | Caso de uso para forçar coleta manual via REST (`POST /trigger`).                                         |

---

## 3. Camada **infra** (adapters)

| Classe / Adapter                | Implementa / Usa        | Observação                                                       |
| ------------------------------- | ----------------------- | ---------------------------------------------------------------- |
| **JpaWatchRouteRepository**     | `WatchRouteRepository`  | Spring Data JPA + tabela `watch_route`.                          |
| **JpaFlightPriceRepository**    | `FlightPriceRepository` | Tabela `flight_price`.                                           |
| **ExternalFlightApiClient**     | —                       | Encapsula chamadas HTTP às APIs de voo (Kiwi, Skyscanner etc.).  |
| **RabbitPriceUpdatedPublisher** | —                       | Publica evento na exchange `price.events`.                       |
| **RabbitAlertCreatedListener**  | —                       | Consome `alert.created`, faz `upsert` em `WatchRouteRepository`. |

---

## 4. Camada **presentation**

| Controller                  | Endpoints                                           | Chamadas ao application              |
| --------------------------- | --------------------------------------------------- | ------------------------------------ |
| **HealthController**        | `GET /health`                                       | —                                    |
| **ManualTriggerController** | `POST /trigger` (JSON com `origin`, `dest`, `date`) | → `ManualTriggerHandler.handle(...)` |

---

## 5. Visão ASCII das dependências centrais

```
                 +------------------------+
                 |  RabbitAlertCreated    |
                 |      Listener          |
                 +-----------+------------+
                             |
                             v
              +--------------+---------------+
              |     WatchRouteRepository     |<-Jpa impl
              +--------------+---------------+
                             ^
      +--------------+       |           +-------------------+
      | PricePolling |-------+------->   | ExternalFlightApi |
      |     Job      |                   +---------+---------+
      +--------------+                             |
             |                                     |
             v                                     v
  +----------+-----------+               +---------+-----------------+
  |   PriceChangeDetector|-------------->| PriceVariationPolicy      |
  |                      |               +---------------------------+
  |  + publish event ----+--> RabbitPriceUpdatedPublisher            |
  +----------------------+
```

---

### Como ler

* **Setas sólidas (→)** indicam injeção de dependência (constructor injection no Spring).
* **Adapters JPA / Rabbit** implementam as *ports* definidas na camada *domain*.
* Nenhuma classe de **domain** conhece Spring, Rabbit ou JPA — mantendo o núcleo isolado.

