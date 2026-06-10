# Draftlab Payment Platform

Учебная распределенная платежная система на Spring Boot, Kafka и PostgreSQL.

## Архитектура

| Компонент | Порт | Ответственность |
|---|---:|---|
| `api-gateway` | 8080 | Единая точка входа, маршрутизация, `X-Correlation-ID` |
| `order-service` | 8081 | Command side заказов, outbox; Query side через отдельный `OrderQueryService` |
| `payment-service` | 8082 | Исполнение payment-команд, защищенный вызов ledger |
| `ledger-service` | 8083 | Идемпотентное резервирование средств |
| `notification-service` | 8084 | Асинхронная обработка уведомлений |
| `saga-orchestrator` | 8085 | Состояние и переходы Saga, команды шагов |

Основной поток:

```text
POST /api/orders
  -> orders.created
  -> saga-orchestrator
  -> payments.commands.authorize
  -> payment-service -> ledger-service
  -> payments.authorized | payments.failed
  -> saga-orchestrator -> notifications.requested
```

## Реализованные паттерны

- API Gateway: Spring Cloud Gateway, внешние запросы идут через `localhost:8080`.
- Circuit Breaker, Retry, Timeout, Bulkhead: Resilience4j вокруг `payment-service -> ledger-service`.
- Saga orchestration: отдельный оркестратор и персистентное состояние `order_sagas`.
- CQRS: команды заказа и read DTO обслуживаются отдельными application services; платежи имеют отдельный query service.
- Distributed Tracing: Micrometer Tracing + OpenTelemetry OTLP + Jaeger.
- Correlation ID: `X-Correlation-ID` создается Gateway, переносится в события и восстанавливается в MDC consumers.
- Transactional Outbox и Idempotent Consumer для at-least-once доставки Kafka.
- Actuator, health probes и Prometheus endpoint во всех сервисах.
- Cache-aside для query-side заказов и платежей.

## Запуск и проверка

Требования: Java 24, Docker Compose и `jq`. Wrapper сам использует подходящую версию Gradle.

### 1. Запустить инфраструктуру и собрать проект

```bash
docker compose up -d
./gradlew clean build
```

Контейнер `database-migrations` автоматически и идемпотентно создает сервисные базы данных,
включая `sagas_db`. Это работает как с новым, так и с уже существующим PostgreSQL volume.
При запуске приложений Liquibase создаст и обновит таблицы внутри соответствующих баз.

Проверить инфраструктуру:

```bash
docker compose ps
docker compose ps -a database-migrations
```

У `database-migrations` ожидается состояние `Exited (0)`.

### 2. Запустить приложения

Запустить приложения в отдельных терминалах. Перед повторным запуском завершите ранее
запущенные `bootRun`, иначе порты уже будут заняты.

```bash
./gradlew :api-gateway:bootRun
./gradlew :order-service:bootRun
./gradlew :payment-service:bootRun
./gradlew :ledger-service:bootRun
./gradlew :notification-service:bootRun
./gradlew :saga-orchestrator:bootRun
```

Проверить health всех приложений:

```bash
for port in 8080 8081 8082 8083 8084 8085; do
  curl -fsS "http://localhost:${port}/actuator/health"
  echo
done
```

Все ответы должны содержать `"status":"UP"`.

### 3. Проверить успешную Saga

Создать заказ через Gateway:

```bash
curl -i -X POST http://localhost:8080/api/orders \
  -H 'Content-Type: application/json' \
  -H 'X-Correlation-ID: saga-success-001' \
  -d '{"customerId":"customer-1","amount":120.50,"currency":"USD"}'
```

Скопировать `id` заказа из JSON-ответа и выполнить:

```bash
ORDER_ID="<UUID>"
```

Обработка асинхронная, поэтому перед проверкой необходимо подождать 1-3 секунды.

Проверить заказ:

```bash
curl -s "http://localhost:8080/api/orders/$ORDER_ID" | jq
```

Ожидаемый статус:

```json
"status": "PAYMENT_AUTHORIZED"
```

Проверить платеж:

```bash
curl -s "http://localhost:8080/api/payments/by-order/$ORDER_ID" | jq
```

Ожидаемый статус:

```json
"status": "AUTHORIZED"
```

Проверить Saga:

```bash
curl -s "http://localhost:8080/api/sagas/by-order/$ORDER_ID" | jq
```

Ожидаемый результат:

```json
{
  "status": "COMPLETED",
  "failureReason": null
}
```

Проверить уведомление:

```bash
curl -s "http://localhost:8080/api/notifications/by-order/$ORDER_ID" | jq
```

Ожидается массив с одной записью.

### 4. Проверить отказоустойчивость

Завершить процесс `ledger-service`.

Создать новый заказ:

```bash
curl -i -X POST http://localhost:8080/api/orders \
  -H 'Content-Type: application/json' \
  -H 'X-Correlation-ID: saga-failure-001' \
  -d '{"customerId":"customer-2","amount":50.00,"currency":"USD"}'
```

Скопировать `id` нового заказа и выполнить:

```bash
FAILED_ORDER_ID="<UUID>"
```

Из-за Retry и асинхронной доставки обработка отказа может занять несколько секунд.

Проверить заказ:

```bash
curl -s "http://localhost:8080/api/orders/$FAILED_ORDER_ID" | jq
```

Ожидаемый статус:

```json
"status": "PAYMENT_FAILED"
```

Проверить платеж:

```bash
curl -s "http://localhost:8080/api/payments/by-order/$FAILED_ORDER_ID" | jq
```

Ожидаемый статус:

```json
"status": "FAILED"
```

Проверить Saga:

```bash
curl -s "http://localhost:8080/api/sagas/by-order/$FAILED_ORDER_ID" | jq
```

Ожидаемый результат:

```json
{
  "status": "FAILED",
  "failureReason": "Ledger is unavailable"
}
```

Проверить, что уведомление об успешном платеже не создано:

```bash
curl -s "http://localhost:8080/api/notifications/by-order/$FAILED_ORDER_ID" | jq
```

Ожидаемый результат:

```json
[]
```

Проверить состояние Circuit Breaker:

```bash
curl -s http://localhost:8082/actuator/circuitbreakers | jq
```

Для накопления окна Circuit Breaker повторите создание отказного заказа несколько раз.
После проверки снова запустите `ledger-service`:

```bash
./gradlew :ledger-service:bootRun
```

### 5. Проверить наблюдаемость

- Jaeger UI: http://localhost:16686

В Jaeger можно фильтровать traces по именам сервисов. Для поиска всех асинхронных частей
одного сценария используйте `saga-success-001` или `saga-failure-001` в логах приложений.
