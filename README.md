# Draftlab Payment Platform

Вымышленная платежная система для внедрения паттернов распределенных систем.

## Сервисы

- `order-service` (`8081`) - принимает заказ, сохраняет его и публикует `orders.created` через Transactional Outbox.
- `payment-service` (`8082`) - обрабатывает `orders.created`, вызывает `ledger-service`, публикует `payments.authorized`, `payments.failed` и `notifications.requested`.
- `ledger-service` (`8083`) - синхронная зависимость для резервирования средств.
- `notification-service` (`8084`) - асинхронно сохраняет уведомления.

## Паттерны

- Saga choreography: заказ запускает цепочку, платеж меняет состояние через события.
- Transactional Outbox: доменное изменение и событие пишутся в одну БД-транзакцию.
- Idempotent Consumer: `processed_messages` защищает consumers от повторной доставки.
- At-least-once delivery: Kafka + идемпотентная обработка.
- Circuit Breaker и Retry: `payment-service -> ledger-service` через Resilience4j.
- Graceful degradation: при недоступном ledger платеж переходит в `FAILED`, система публикует событие отказа.
- Request/reply: HTTP/Feign вызов ledger.
- Publish/subscribe: Kafka topics между сервисами.

## Локальный запуск

```bash
docker compose up -d
gradle clean build
```

В отдельных терминалах:

```bash
gradle :ledger-service:bootRun
gradle :order-service:bootRun
gradle :payment-service:bootRun
gradle :notification-service:bootRun
```

Создать заказ:

```bash
curl -X POST http://localhost:8081/api/orders \
  -H 'Content-Type: application/json' \
  -d '{"customerId":"customer-1","amount":120.50,"currency":"USD"}'
```

Swagger UI:

- http://localhost:8081/swagger-ui.html
- http://localhost:8082/swagger-ui.html
- http://localhost:8083/swagger-ui.html
- http://localhost:8084/swagger-ui.html

## Требования

- Java 24
- Gradle 8.14 или новее
- Docker / Docker Compose
