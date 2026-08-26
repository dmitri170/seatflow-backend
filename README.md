# SeatFlow Backend

SeatFlow — платформа, на которой организаторы создают события и схемы залов, а пользователи находят события, выбирают места, временно бронируют их, оплачивают билеты и получают уведомления.

## Требования

- Java 21

## Запуск приложения

```powershell
.\mvnw.cmd spring-boot:run
```

## Запуск тестов

```powershell
.\mvnw.cmd clean verify
```

## Доступные endpoint

### Проверка приложения

```http
GET /api/v1/system/ping
```

Пример ответа:

```json
{
  "status": "UP",
  "service": "seatflow-backend"
}
```

### Health Check

```http
GET /actuator/health
```

Пример ответа:

```json
{
  "status": "UP"
}
```