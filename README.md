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
