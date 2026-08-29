# SeatFlow Backend

SeatFlow — платформа, на которой организаторы создают события и схемы залов, а пользователи находят события, выбирают
места, временно бронируют их, оплачивают билеты и получают уведомления.

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

## Локальная база данных PostgreSQL

Для запуска базы данных требуется установленный и запущенный Docker Desktop.

Создайте локальный файл с переменными окружения:

```powershell
Copy-Item .env.example .env
```

Запустите PostgreSQL:

```powershell
docker compose up -d postgres
```

Проверьте состояние контейнера:

```powershell
docker compose ps
```

Контейнер должен перейти в состояние `healthy`.

Проверьте подключение к базе данных:

```powershell
docker compose exec postgres psql -U seatflow -d seatflow -c "SELECT 1 AS result;"
```

Остановите контейнер:

```powershell
docker compose down
```

## Миграции базы данных

Для управления схемой базы данных используется Flyway.

Миграции находятся в каталоге:

```text
src/main/resources/db/migration
```

При запуске приложения Flyway автоматически применяет новые миграции. Первая миграция:

```text
V1__create_seatflow_schema.sql
```

Она создаёт схему `seatflow`. История выполненных миграций хранится в таблице:

```text
public.flyway_schema_history
```

Интеграционный тест использует Testcontainers и запускает отдельный PostgreSQL-контейнер на случайном свободном порту.
Для выполнения тестов Docker Desktop должен быть запущен.

## Формат ошибок API

Все ошибки API возвращаются в едином формате:

```json
{
  "timestamp": "2026-08-29T00:30:00Z",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "path": "/api/v1/venues",
  "fieldErrors": [
    {
      "field": "name",
      "message": "must not be blank"
    }
  ]
}
```

Поддерживаемые коды ошибок:

| Код                  | HTTP-статус | Описание                         |
|----------------------|------------:|----------------------------------|
| `VALIDATION_ERROR`   |         400 | Ошибка проверки входных данных   |
| `MALFORMED_REQUEST`  |         400 | Некорректный JSON                |
| `RESOURCE_NOT_FOUND` |         404 | Ресурс или endpoint не найден    |
| `INTERNAL_ERROR`     |         500 | Непредвиденная внутренняя ошибка |
| `METHOD_NOT_ALLOWED` |         405 | HTTP-метод не поддерживается     |

Для внутренних ошибок клиенту не возвращаются stack trace и технические детали исключения.

## Venue API

API позволяет создавать и получать площадки проведения событий.

### Создание площадки

```http
POST /api/v1/venues
Content-Type: application/json
```

Пример запроса:

```json
{
  "name": "Luzhniki Stadium",
  "city": "Moscow",
  "address": "Luzhnetskaya Naberezhnaya, 24",
  "timezone": "Europe/Moscow"
}
```

Успешный запрос возвращает `201 Created` и заголовок:

```text
Location: /api/v1/venues/{id}
```

Часовой пояс должен быть корректным идентификатором IANA, например `Europe/Moscow`.

### Получение площадки

```http
GET /api/v1/venues/{id}
```

### Получение списка площадок

```http
GET /api/v1/venues
```