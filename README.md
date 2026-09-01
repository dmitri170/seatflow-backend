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
POST /api/v1/venues/{venueId}/halls
GET  /api/v1/venues/{venueId}/halls
GET  /api/v1/halls/{hallId}
```

## API documentation

После запуска приложения документация API доступна по следующим адресам:

* Swagger UI: http://localhost:18080/swagger-ui/index.html
* OpenAPI JSON: http://localhost:18080/v3/api-docs

Если приложение запущено на другом порту, замените `18080` на используемый порт.

## API conventions

* базовый путь REST API — `/api/v1`;
* запросы и ответы передаются в формате JSON;
* идентификаторы ресурсов имеют формат UUID;
* дата и время передаются в UTC в формате ISO 8601;
* успешное создание ресурса возвращает `201 Created`;
* при наличии endpoint для получения созданного ресурса сервер может возвращать заголовок `Location`;
* ошибки возвращаются в едином формате `ApiErrorResponse`.

### HTTP-статусы

| Статус                      | Описание                                     |
|-----------------------------| -------------------------------------------- |
| `200 OK`                    | Запрос успешно выполнен                      |
| `201 Created`               | Ресурс успешно создан                        |
| `400 Bad Request`           | Некорректный JSON, UUID или ошибка валидации |
| `404 Not Found`             | Запрашиваемый ресурс не найден               |
| `409 Conflict`              | Ресурс конфликтует с существующими данными   |
| `500 Internal Server Error` | Непредвиденная ошибка сервера                |

### Формат ошибки

```json
{
  "timestamp": "2026-08-31T12:00:00Z",
  "status": 404,
  "code": "RESOURCE_NOT_FOUND",
  "message": "Venue not found",
  "path": "/api/v1/venues/3d03415e-2494-410f-b6f6-12b6e3610290",
  "fieldErrors": []
}
```
## Sector API

API позволяет создавать сектора внутри залов и получать список секторов конкретного зала.

### Создание сектора

```http
POST /api/v1/halls/{hallId}/sectors
Content-Type: application/json
```

Пример запроса:

```json
{
  "name": "VIP",
  "rowCount": 10,
  "seatsPerRow": 20
}
```

При создании сектора SeatFlow автоматически генерирует все места на основе количества рядов `rowCount` и количества мест в ряду `seatsPerRow`.

Например:

```text
rowCount = 10
seatsPerRow = 20

Общее количество мест = 200
```

Успешный запрос возвращает `201 Created`.

### Получение секторов зала

```http
GET /api/v1/halls/{hallId}/sectors
```

Возвращает список всех секторов указанного зала.

## Seat API

API позволяет получать места, принадлежащие конкретному сектору.

### Получение мест сектора

```http
GET /api/v1/sectors/{sectorId}/seats
```

Места возвращаются отсортированными сначала по номеру ряда, затем по номеру места.

Например:

```text
Ряд 1: место 1, место 2, место 3, ...
Ряд 2: место 1, место 2, место 3, ...
```

## Локальный порт приложения

По умолчанию приложение запускается по адресу:

```text
http://localhost:18080
```

Порт можно переопределить с помощью переменной окружения `SERVER_PORT`.

Swagger UI:

```text
http://localhost:18080/swagger-ui/index.html
```

OpenAPI JSON:

```text
http://localhost:18080/v3/api-docs
```
