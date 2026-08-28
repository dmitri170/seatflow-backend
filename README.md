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

Интеграционный тест использует Testcontainers и запускает отдельный PostgreSQL-контейнер на случайном свободном порту. Для выполнения тестов Docker Desktop должен быть запущен.