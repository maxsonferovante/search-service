# Search Service - Flight Price Monitor

O **Search Service** é um microsserviço responsável por coletar preços de voos em intervalos regulares, detectar variações significativas e publicar eventos quando os preços caem abaixo dos limites definidos pelos usuários.

## Visão Geral

Este serviço implementa uma arquitetura hexagonal (Ports & Adapters) com Clean Architecture, mantendo o domínio isolado de frameworks e dependências externas. O serviço:

- 🔄 Coleta preços automaticamente via job agendado
- 💾 Persiste histórico de preços no PostgreSQL
- 📊 Detecta variações significativas usando políticas de negócio
- 📢 Publica eventos via RabbitMQ quando há quedas relevantes
- 🎯 Monitora rotas específicas baseadas em alertas criados pelos usuários

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
| **PriceUpdated** *(evento de domínio)* | • `messageId: UUID`<br>• `route: String`<br>• `date: LocalDate`<br>• `oldPrice: BigDecimal`<br>• `newPrice: BigDecimal`<br>• `currency: Currency`<br>• `checkedAt: Instant`                                                    | Evento publicado quando há variação significativa.      |
| **PriceVariationPolicy**               | `boolean isSignificantDrop(oldPrice, newPrice, tolerance)`                                                                                                                                                                       | Regra de negócio que decide se a queda é relevante.     |
| **WatchRouteRepository** *(porta)*     | `List<WatchRoute> findAllActive()`<br>`void upsert(WatchRoute)`                                                                                                                                                                  | Interface para CRUD das rotas vigiadas.                 |
| **FlightPriceRepository** *(porta)*    | `void save(FlightPrice)`<br>`Optional<FlightPrice> findLatest(origin, dest, date)`                                                                                                                                               | Interface para persistência do histórico de preços.    |

---

## 2. Camada **Application** (`com.maal.searchservice.application`)

| Classe                             | Dependências (→)                                                                             | Responsabilidade                                                                                          |
| ---------------------------------- | -------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------- |
| **PricePollingJob** *(@Scheduled)* | → `WatchRouteRepository`<br>→ `ExternalFlightApiClient`<br>→ `PriceChangeDetector`           | Executa periodicamente: coleta rotas ativas, chama API externa e delega detecção de variação.            |
| **PriceChangeDetector**            | → `FlightPriceRepository`<br>→ `PriceVariationPolicy`<br>→ `RabbitPriceUpdatedPublisher`     | Salva preço novo, aplica política de variação e publica `PriceUpdated` se houver queda significativa.     |
| **ManualTriggerHandler**           | → `ExternalFlightApiClient`<br>→ `PriceChangeDetector`                                       | Caso de uso para trigger manual de coleta via endpoint REST.                                             |

---

## 3. Camada **Infrastructure** (`com.maal.searchservice.infra`)

### 3.1 Persistência (`infra.persistence`)
| Classe / Adapter                | Implementa / Usa        | Observação                                                       |
| ------------------------------- | ----------------------- | ---------------------------------------------------------------- |
| **JpaWatchRouteRepository**     | `WatchRouteRepository`  | Spring Data JPA + entidade `WatchRouteEntity`.                   |
| **JpaFlightPriceRepository**    | `FlightPriceRepository` | Persiste `FlightPriceEntity` com histórico completo.             |

### 3.2 APIs Externas (`infra.api`)
| Classe / Adapter                | Implementa / Usa        | Observação                                                       |
| ------------------------------- | ----------------------- | ---------------------------------------------------------------- |
| **ExternalFlightApiClient**     | —                       | Cliente HTTP para APIs de voo (SerpAPI/Google Flights).          |

### 3.3 Messaging (`infra.messaging`)
| Classe / Adapter                | Implementa / Usa        | Observação                                                       |
| ------------------------------- | ----------------------- | ---------------------------------------------------------------- |
| **RabbitPriceUpdatedPublisher** | —                       | Publica `PriceUpdated` na exchange `price.events`.               |
| **RabbitAlertCreatedListener**  | —                       | Consome `alert.created`, faz upsert em `WatchRouteRepository`.    |

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

### Persistence & Messaging
- **PostgreSQL** - Banco de dados principal
- **RabbitMQ** - Message broker para eventos
- **Hibernate** - ORM com dialeto PostgreSQL

### External APIs
- **Spring Cloud OpenFeign** - Cliente HTTP declarativo
- **SerpAPI** - Google Flights integration

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
spring.jpa.hibernate.ddl-auto=update
```

### API Externa (Google Flights via SerpAPI)
```properties
external.flight.api.url=https://serpapi.com/search.json
external.flight.api.key=${SERPAPI_KEY}
external.flight.api.engine=google_flights
external.flight.api.currency=USD
external.flight.api.language=en
```

---

## 7. Fluxo de Dados

```
┌──────────────┐    Schedule    ┌─────────────────┐    HTTP    ┌─────────────────┐
│ PricePolling │ ──────────────► │ ExternalFlight  │ ─────────► │ Google Flights  │
│     Job      │                │   ApiClient     │            │   (SerpAPI)     │
└──────────────┘                └─────────────────┘            └─────────────────┘
        │                                │
        ▼                                ▼
┌──────────────┐                ┌─────────────────┐
│ ChangeDetect │                │   FlightPrice   │
│   Service    │ ──────────────► │   Repository    │
└──────────────┘      Save      └─────────────────┘
        │
        ▼ (se queda significativa)
┌──────────────┐    Publish     ┌─────────────────┐    Consume  ┌─────────────────┐
│ RabbitPrice  │ ──────────────► │    RabbitMQ     │ ──────────► │   Alert Service │
│ Publisher    │                │ (price.events)   │            │                 │
└──────────────┘                └─────────────────┘            └─────────────────┘
```

---

## 8. Estrutura de Diretórios

```
search-service/
├── build.gradle.kts
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
│   │   │   │   ├── politics/             # Políticas de negócio
│   │   │   │   └── events/               # Eventos de domínio
│   │   │   │
│   │   │   ├── application/              # Casos de uso
│   │   │   │   ├── scheduler/           # Jobs agendados
│   │   │   │   ├── service/             # Serviços de aplicação
│   │   │   │   └── command/             # Comandos/Handlers
│   │   │   │
│   │   │   ├── infra/                   # Adaptadores externos
│   │   │   │   ├── api/                 # Clientes de APIs externas
│   │   │   │   ├── persistence/         # Repositórios JPA
│   │   │   │   └── messaging/           # RabbitMQ publishers/listeners
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
├── docker-compose.yml                  # PostgreSQL + RabbitMQ para desenvolvimento
└── README.md
```

---

## 9. Exemplo de Evento Publicado

```json
{
  "messageId": "b1fc7199-3f0d-11ef-bd87-0242ac120005",
  "route": "GRU-CDG",
  "date": "2025-07-10",
  "oldPrice": 2500.00,
  "newPrice": 1899.99,
  "currency": "BRL",
  "checkedAt": "2025-05-15T12:30:05Z"
}
```

---

## 10. Principles Applied

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
```bash
# 1. Subir dependências
docker-compose up -d

# 2. Executar aplicação
./gradlew bootRun

# 3. Health check
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

