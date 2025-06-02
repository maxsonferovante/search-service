# Search Service - Flight Price Monitor

O **Search Service** é um microsserviço responsável por coletar preços de voos em intervalos regulares, detectar variações significativas e publicar eventos quando os preços caem abaixo dos limites definidos pelos usuários.

## Visão Geral

Este serviço implementa uma arquitetura hexagonal (Ports & Adapters) com Clean Architecture, mantendo o domínio isolado de frameworks e dependências externas. O serviço:

- 🔄 Coleta preços automaticamente via job agendado
- 💾 Persiste histórico de preços no PostgreSQL
- 📊 Detecta variações significativas usando políticas de negócio
- 📢 Publica eventos via RabbitMQ quando há quedas relevantes
- 🎯 Monitora rotas específicas baseadas em alertas criados pelos usuários
- 🧪 Inclui API mock para desenvolvimento e testes

---

## Arquitetura Hexagonal

```
                         ┌─────────────────┐
                         │   Domain Core   │
                         │                 │
                         │  • WatchRoute   │
                         │  • FlightPrice  │
                         │  • PriceUpdated │
                         │  • Policies     │
                         └─────────────────┘
                                  ↑
                 ┌────────────────┼────────────────┐
                 │                │                │
        ┌────────▼───────┐ ┌──────▼──────┐ ┌──────▼──────┐
        │  Application   │ │Presentation │ │Infrastructure│
        │                │ │             │ │             │
        │ • PricePolling │ │ • REST API  │ │ • JPA       │
        │ • ChangeDetect │ │ • Events    │ │ • RabbitMQ  │
        │ • Commands     │ │ • Mappers   │ │ • External  │
        └────────────────┘ └─────────────┘ └─────────────┘
```

---

## 1. Camada **Domain** (`com.maal.searchservice.domain`)

| Classe / Interface                     | Membros                                                                                                                                                                                                                          | Papel                                                    |
| -------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------- |
| **WatchRoute**                         | • `alertId: Long`<br>• `origin: String`<br>• `destination: String`<br>• `outboundDate: LocalDate`<br>• `returnDate: LocalDate`<br>• `targetPrice: BigDecimal`<br>• `toleranceUp: BigDecimal`<br>• `currency: Currency`<br>• `active: Boolean` | Representa a rota e condições que estão sendo vigiadas. |
| **FlightPrice**                        | • `origin: String`<br>• `destination: String`<br>• `travelDate: LocalDate`<br>• `price: BigDecimal`<br>• `currency: Currency`<br>• `checkedAt: Instant`                                                                         | Snapshot do preço coletado em um momento específico.    |
| **PriceUpdated** *(evento de domínio)* | • `messageId: UUID`<br>• `alertId: Long`<br>• `origin: String`<br>• `destination: String`<br>• `outboundDate: LocalDate`<br>• `returnDate: LocalDate`<br>• `oldPrice: BigDecimal`<br>• `newPrice: BigDecimal`<br>• `currency: Currency`<br>• `targetPrice: BigDecimal`<br>• `toleranceUp: BigDecimal`<br>• `checkedAt: Instant` | Evento publicado quando há variação significativa.      |
| **AlertEventPayload** *(evento)*      | • `messageId: UUID`<br>• `origin: String`<br>• `destination: String`<br>• `outboundDate: LocalDate`<br>• `returnDate: LocalDate`<br>• `oldPrice: BigDecimal`<br>• `newPrice: BigDecimal`<br>• `currency: Currency`<br>• `checkedAt: Instant` | Payload do evento de alerta para publicação.           |
| **PriceVariationPolicy**               | `Boolean isSignificantDrop(oldPrice, newPrice, tolerance)`                                                                                                                                                                       | Regra de negócio que decide se a queda é relevante.     |
| **MessagingException**                 | Extends `RuntimeException`                                                                                                                                                                                                        | Exceção específica para problemas de messaging.         |
| **WatchRouteRepository** *(porta)*     | `List<WatchRoute> findAllActive()`<br>`void upsert(WatchRoute)`<br>`Optional<WatchRoute> findByAlertId(Long)`<br>`Optional<WatchRoute> findById(Long)`<br>`void deleteById(Long)`<br>`void deleteByAlertId(Long)`            | Interface para CRUD das rotas vigiadas.                 |
| **FlightRepository** *(porta)*         | `void save(FlightPrice)`<br>`Optional<FlightPrice> findLatest(origin, dest, date)`<br>Operações de busca e persistência                                                                                                          | Interface para persistência do histórico de preços.    |
| **PriceHistoryRepository** *(porta)*   | `void save(PriceUpdated)`                                                                                                                                                                                                         | Interface para persistência do histórico de eventos.   |
| **PriceAlertPublisher** *(porta)*      | `void publishPriceAlert(AlertEventPayload)`                                                                                                                                                                                      | Interface para publicação de alertas de preços.        |

---

## 2. Camada **Application** (`com.maal.searchservice.application`)

| Classe                               | Dependências (→)                                                                             | Responsabilidade                                                                                          |
| ------------------------------------ | -------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------- |
| **PricePollingJob** *(@Scheduled)*   | → `WatchRouteRepository`<br>→ `ExternalFlightApiClient`<br>→ `PriceDropOrchestrator`         | Executa periodicamente: coleta rotas ativas, chama API externa e delega orquestração.                   |
| **PriceDropOrchestrator**            | → `PriceChangeDetector`<br>→ `PriceHistoryRepository`<br>→ `PriceAlertPublisher`<br>→ `PriceUpdatedMapper` | Orquestra o fluxo de detecção de quedas e publicação de alertas.                                         |
| **PriceChangeDetector**              | → `FlightRepository`<br>→ `PriceVariationPolicy`                                            | Detecta variações significativas comparando preços atuais com histórico.                                 |
| **PriceUpdatedMapper**               | —                                                                                            | Converte eventos de domínio para payloads de alerta.                                                     |
| **TriggerManualFetchCommand**        | —                                                                                            | Comando para trigger manual de coleta (DTO/Record).                                                      |

---

## 3. Camada **Infrastructure** (`com.maal.searchservice.infra`)

### 3.1 Persistência (`infra.persistence`)

#### Entidades JPA
| Entidade                       | Tabela                  | Responsabilidade                                                  |
| ------------------------------ | ----------------------- | ----------------------------------------------------------------- |
| **WatchRouteEntity**           | `watch_routes`          | Persistência de rotas sendo monitoradas.                         |
| **FlightPriceEntity**          | `flight_prices`         | Histórico completo de preços coletados.                          |
| **PriceUpdatedEntity**         | `price_history`         | Eventos de alteração de preços persistidos.                      |

#### Repositórios Spring Data JPA
| Repositório                    | Entidade                | Operações Principais                                              |
| ------------------------------ | ----------------------- | ----------------------------------------------------------------- |
| **JpaWatchRouteRepository**    | `WatchRouteEntity`      | CRUD, busca por alertId, busca de rotas ativas.                  |
| **JpaFlightPriceRepository**   | `FlightPriceEntity`     | Salvamento e busca de preços por rota e data.                    |
| **JpaPriceHistoryRepository**  | `PriceUpdatedEntity`    | Persistência do histórico de eventos de preços.                  |

#### Adaptadores de Persistência
| Adapter                        | Implementa              | Responsabilidade                                                  |
| ------------------------------ | ----------------------- | ----------------------------------------------------------------- |
| **JpaWatchRouteAdapter**       | `WatchRouteRepository`  | Converte entre domain models e entities para rotas.              |
| **JpaFlightPriceAdapter**      | `FlightRepository`      | Converte entre domain models e entities para preços.             |
| **JpaPriceUpdatedAdapter**     | `PriceHistoryRepository`| Converte entre domain models e entities para histórico.          |

#### Mappers de Persistência
| Mapper                         | Conversão               | Observação                                                        |
| ------------------------------ | ----------------------- | ----------------------------------------------------------------- |
| **WatchRouteMapper**           | `WatchRoute` ↔ `Entity` | Conversão bidirecional com validações.                           |
| **FlightPriceMapper**          | `FlightPrice` ↔ `Entity`| Conversão bidirecional preservando timestamps.                   |
| **PriceHistoryMapper**         | `PriceUpdated` ↔ `Entity`| Conversão de eventos para persistência.                          |

### 3.2 APIs Externas (`infra.api`)
| Classe / Adapter                | Implementa / Usa        | Observação                                                       |
| ------------------------------- | ----------------------- | ---------------------------------------------------------------- |
| **ExternalFlightApiClient**     | —                       | Cliente HTTP para APIs de voo (Mock API local para desenvolvimento). |
| **ExternalFlightApiConfig**     | —                       | Configuração do cliente Feign para API externa.                  |

### 3.3 Messaging (`infra.messaging`)
| Classe / Adapter                | Implementa / Usa        | Observação                                                       |
| ------------------------------- | ----------------------- | ---------------------------------------------------------------- |
| **RabbitPriceAlertPublisher**   | —                       | Publica `PriceUpdated` na exchange `price.events`.               |

---

## 4. Camada **Presentation** (`com.maal.searchservice.presentation`)

### 4.1 REST Controllers (`presentation.rest`)
| Controller                  | Endpoints                                           | Chamadas ao Application              |
| --------------------------- | --------------------------------------------------- | ------------------------------------ |
| **HealthController**        | `GET /health`                                       | —                                    |
| **ManualTriggerController** | `POST /trigger` (JSON com `origin`, `dest`, `date`) | → `ManualTriggerHandler.handle(...)` |

### 4.2 Event Handlers (`presentation.event`)
| Handler                     | Eventos                     | Responsabilidade                     |
| --------------------------- | --------------------------- | ------------------------------------ |
| **AlertCreatedHandler**     | `alert.created`             | Processa alertas criados pelo Alert Service |

### 4.3 Mappers (`presentation.mapper`)
| Mapper                      | Conversões                  | Responsabilidade                     |
| --------------------------- | --------------------------- | ------------------------------------ |
| **FlightPriceMapper**       | DTO ↔ Domain Model          | Converte entre camadas sem vazamentos |

---

## 5. Tecnologias e Dependências

### Core Framework
- **Spring Boot 3.4.5** (Java 21)
- **Spring Data JPA** - Persistência
- **Spring AMQP** - Messaging com RabbitMQ
- **Spring Web** - REST APIs
- **Virtual Threads** - Habilitado para melhor performance

### Persistence & Messaging
- **PostgreSQL** - Banco de dados principal
- **RabbitMQ** - Message broker para eventos
- **Hibernate** - ORM com dialeto PostgreSQL

### External APIs
- **Spring Cloud OpenFeign 4.2.1** - Cliente HTTP declarativo
- **Mock API Python** - API local para desenvolvimento e testes

### Development
- **Lombok** - Redução de boilerplate
- **Spring Boot Test** - Testes integrados

---

## 6. Configuração

### Banco de Dados
```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/search}
spring.datasource.username=${DB_USERNAME:admin}
spring.datasource.password=${DB_PASSWORD:admin}
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

### API Externa (Mock Local)
```properties
external.flight.api.url=http://localhost:5002/search_flights
external.flight.api.key="SUA_API_KEY"
external.flight.api.engine=google_flights
external.flight.api.currency=USD
external.flight.api.language=en
```

### RabbitMQ
```properties
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=${RABBIT_USER:guest}
spring.rabbitmq.password=${RABBIT_PASS:guest}
```

---

## 7. Fluxo de Dados

```
┌──────────────┐    Schedule    ┌─────────────────┐    HTTP    ┌─────────────────┐
│ PricePolling │ ──────────────► │ ExternalFlight  │ ─────────► │   Mock API      │
│     Job      │                │   ApiClient     │            │  (Python)       │
└──────────────┘                └─────────────────┘            └─────────────────┘
        │                                │
        ▼                                ▼
┌──────────────┐    delegate    ┌─────────────────┐    compare  ┌─────────────────┐
│ PriceDrop    │ ──────────────► │ PriceChange     │ ──────────► │ FlightPrice     │
│ Orchestrator │                │   Detector      │             │   Repository    │
└──────────────┘                └─────────────────┘             └─────────────────┘
        │                                │
        ▼ (save history)                 ▼ (if significant drop)
┌──────────────┐                ┌─────────────────┐
│ PriceHistory │                │ PriceVariation  │
│  Repository  │                │     Policy      │
└──────────────┘                └─────────────────┘
        │
        ▼ (publish alert)
┌──────────────┐    Publish     ┌─────────────────┐    Consume  ┌─────────────────┐
│ PriceAlert   │ ──────────────► │    RabbitMQ     │ ──────────► │   Alert Service │
│  Publisher   │                │ (price.events)   │            │                 │
└──────────────┘                └─────────────────┘            └─────────────────┘
```

---

## 8. Estrutura de Diretórios Atual

```
search-service/
├── build.gradle.kts
├── compose.yml                         # PostgreSQL + RabbitMQ para desenvolvimento
├── api.py                             # Mock API Python para desenvolvimento
├── src/
│   ├── main/
│   │   ├── java/com/maal/searchservice/
│   │   │   ├── SearchServiceApplication.java
│   │   │   ├── domain/                    # Núcleo do negócio
│   │   │   │   ├── modal/                # Modelos de domínio
│   │   │   │   │   ├── WatchRoute.java
│   │   │   │   │   ├── FlightPrice.java
│   │   │   │   │   └── PriceUpdated.java
│   │   │   │   ├── repository/           # Interfaces (ports)
│   │   │   │   │   ├── WatchRouteRepository.java
│   │   │   │   │   ├── FlightRepository.java
│   │   │   │   │   └── PriceHistoryRepository.java
│   │   │   │   ├── port/                 # Portas da arquitetura hexagonal
│   │   │   │   │   └── PriceAlertPublisher.java
│   │   │   │   ├── politics/             # Políticas de negócio
│   │   │   │   │   └── PriceVariationPolicy.java
│   │   │   │   ├── event/                # Eventos de domínio
│   │   │   │   │   └── AlertEventPayload.java
│   │   │   │   └── exception/            # Exceções de domínio
│   │   │   │       └── MessagingException.java
│   │   │   │
│   │   │   ├── application/              # Casos de uso
│   │   │   │   ├── scheduler/           # Jobs agendados
│   │   │   │   │   └── PricePollingJob.java
│   │   │   │   ├── service/             # Serviços de aplicação
│   │   │   │   │   ├── PriceDropOrchestrator.java
│   │   │   │   │   └── PriceChangeDetector.java
│   │   │   │   ├── command/             # Comandos/Handlers
│   │   │   │   │   └── TriggerManualFetchCommand.java
│   │   │   │   └── mapper/              # Mapeadores de aplicação
│   │   │   │       └── PriceUpdatedMapper.java
│   │   │   │
│   │   │   ├── infra/                   # Adaptadores externos
│   │   │   │   ├── api/                 # Clientes de APIs externas
│   │   │   │   │   ├── ExternalFlightApiClient.java
│   │   │   │   │   ├── ExternalFlightApiConfig.java
│   │   │   │   │   └── dto/             # DTOs para APIs externas
│   │   │   │   ├── persistence/         # Repositórios JPA
│   │   │   │   │   ├── entity/          # Entidades JPA
│   │   │   │   │   │   ├── WatchRouteEntity.java
│   │   │   │   │   │   ├── FlightPriceEntity.java
│   │   │   │   │   │   └── PriceUpdatedEntity.java
│   │   │   │   │   ├── repository/      # Repositórios Spring Data
│   │   │   │   │   │   ├── JpaWatchRouteRepository.java
│   │   │   │   │   │   ├── JpaFlightPriceRepository.java
│   │   │   │   │   │   └── JpaPriceHistoryRepository.java
│   │   │   │   │   ├── adapter/         # Adaptadores de persistência
│   │   │   │   │   │   ├── JpaWatchRouteAdapter.java
│   │   │   │   │   │   ├── JpaFlightPriceAdapter.java
│   │   │   │   │   │   └── JpaPriceUpdatedAdapter.java
│   │   │   │   │   └── mapper/          # Mapeadores de persistência
│   │   │   │   │       ├── WatchRouteMapper.java
│   │   │   │   │       ├── FlightPriceMapper.java
│   │   │   │   │       └── PriceHistoryMapper.java
│   │   │   │   └── messaging/           # RabbitMQ publishers/listeners
│   │   │   │       └── RabbitPriceAlertPublisher.java
│   │   │   │
│   │   │   ├── presentation/            # Interface externa
│   │   │   │   ├── rest/                # Controllers REST
│   │   │   │   ├── event/               # Event handlers
│   │   │   │   └── mapper/              # DTOs e conversores
│   │   │   │
│   │   │   └── config/                  # Configuração e beans
│   │   │
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── init.sql                # Scripts de inicialização
│   │       ├── static/
│   │       └── templates/
│   │
│   └── test/                           # Testes organizados por camada
│
├── docker-compose.yml                  # Alias para compose.yml
└── README.md
```

---

## 9. Mock API para Desenvolvimento

O projeto inclui uma API mock em Python (`api.py`) que simula o comportamento de APIs de voo reais:

### Características da Mock API:
- **Endpoint**: `GET /search_flights`
- **Porta**: 5002
- **Dados**: 35+ rotas pré-configuradas com preços aleatórios
- **Parâmetros**: `departure_id`, `arrival_id`, `outbound_date`, `return_date`, `currency`
- **Resposta**: JSON com detalhes completos de voos, incluindo escalas e preços

### Executar Mock API:
```bash
python api.py
# API disponível em http://localhost:5002
```

---

## 10. Exemplo de Evento Publicado

### AlertEventPayload (Evento Simplificado)
```json
{
  "messageId": "b1fc7199-3f0d-11ef-bd87-0242ac120005",
  "origin": "GRU",
  "destination": "CDG",
  "outboundDate": "2025-07-10",
  "returnDate": "2025-07-20",
  "oldPrice": 2500.00,
  "newPrice": 1899.99,
  "currency": "BRL",
  "checkedAt": "2025-05-15T12:30:05Z"
}
```

### PriceUpdated (Evento Completo de Domínio)
```json
{
  "messageId": "b1fc7199-3f0d-11ef-bd87-0242ac120005",
  "alertId": 101,
  "origin": "GRU",
  "destination": "CDG",
  "outboundDate": "2025-07-10",
  "returnDate": "2025-07-20",
  "oldPrice": 2500.00,
  "newPrice": 1899.99,
  "currency": "BRL",
  "targetPrice": 2000.00,
  "toleranceUp": 100.00,
  "checkedAt": "2025-05-15T12:30:05Z"
}
```

---

## 11. Principles Applied

### Clean Architecture
- **Dependency Inversion**: Domain não conhece frameworks
- **Interface Segregation**: Repositories como ports bem definidas
- **Single Responsibility**: Cada camada tem responsabilidade única

### Domain-Driven Design
- **Aggregate Roots**: `WatchRoute` e `FlightPrice`
- **Domain Events**: `PriceUpdated` para comunicação assíncrona
- **Policies**: `PriceVariationPolicy` encapsula regras de negócio

### SOLID Principles
- **Open/Closed**: Extensível via novos adapters
- **Liskov Substitution**: Implementations intercambiáveis
- **Dependency Inversion**: Abstrações estáveis, detalhes flexíveis

---

## Como Executar

### Desenvolvimento Local

#### 1. Subir dependências (PostgreSQL + RabbitMQ)
```bash
docker compose up -d
```

#### 2. Executar Mock API (opcional, para desenvolvimento)
```bash
python api.py
```

#### 3. Executar aplicação
```bash
./gradlew bootRun
```

#### 4. Health check
```bash
curl http://localhost:8080/health
```

### Build & Deploy
```bash
# Build da aplicação
./gradlew build

# Build da imagem Docker
./gradlew bootBuildImage

# Deploy (exemplo)
docker run -p 8080:8080 search-service:latest
```

### Configuração de Ambiente

#### Variáveis de Ambiente Disponíveis:
- `DB_URL`: URL do banco PostgreSQL
- `DB_USERNAME`: Usuário do banco
- `DB_PASSWORD`: Senha do banco
- `RABBIT_USER`: Usuário do RabbitMQ
- `RABBIT_PASS`: Senha do RabbitMQ

#### Exemplo de configuração para produção:
```bash
export DB_URL=jdbc:postgresql://prod-db:5432/search
export DB_USERNAME=search_user
export DB_PASSWORD=secure_password
export RABBIT_USER=search_service
export RABBIT_PASS=rabbit_password
```

