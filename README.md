# Bug Report Service

Микросервис для учёта багов и работы с ними.

## Технологии

- Java 25
- Spring Boot 3.5.0
- PostgreSQL
- Liquibase (миграции БД)
- Bucket4j (rate limiting)
- MapStruct (маппинг DTO)
- Lombok

## Запуск

### Требования

- JDK 25
- PostgreSQL 14+
- Maven 3.9+ (или используйте встроенный `mvnw.cmd`)

### Настройка БД

```sql
CREATE DATABASE bug_report_db;
```

### Запуск приложения

```bash
mvnw.cmd spring-boot:run
```

Приложение запустится на порту `8080`.

## API

### Создание баг-репорта

```
POST /api/v1/bugs
Content-Type: multipart/form-data
```

Параметры:
- `request` (JSON part) — данные бага:
  - `description` (обязательно) — описание бага (до 2000 символов)
  - `url` (опционально) — URL страницы, где обнаружен баг
  - `sourcePlatform` (обязательно) — платформа: `SITE_5E24`, `SITE_5E14`, `VTTG`
  - `userLogin` (опционально) — логин авторизованного пользователя
  - `sessionId` (опционально) — ID сессии для неавторизованных
- `screenshot` (файл, опционально) — скриншот бага (до 10MB)

### Получение баг-репорта по ID

```
GET /api/v1/bugs/{id}
```

### Список баг-репортов (с фильтрацией и пагинацией)

```
GET /api/v1/bugs?status=NEW&sourcePlatform=SITE_5E24&page=0&size=20
```

### Обновление статуса

```
PATCH /api/v1/bugs/{id}/status
Content-Type: application/json

{
  "status": "FIXED"
}
```

Доступные статусы: `NEW`, `FIXED`, `REJECTED`

## Rate Limiting

| Тип пользователя | Лимит |
|-------------------|-------|
| Авторизованный (userLogin) | 1 баг в 10 секунд |
| Неавторизованный (sessionId) | 1 баг в минуту |

## Платформы-источники

| Enum | Описание |
|------|----------|
| `SITE_5E24` | Сайт 5e24 |
| `SITE_5E14` | Сайт 5e14 |
| `VTTG` | VTTG |

## Структура проекта

```
src/main/java/com/bugtracker/
├── BugReportServiceApplication.java
├── controller/BugReportController.java
├── dto/
│   ├── BugReportCreateRequest.java
│   ├── BugReportResponse.java
│   └── BugReportUpdateStatusRequest.java
├── exception/
│   ├── BugReportNotFoundException.java
│   ├── FileStorageException.java
│   ├── GlobalExceptionHandler.java
│   └── RateLimitExceededException.java
├── mapper/BugReportMapper.java
├── model/
│   ├── BugReport.java
│   ├── BugStatus.java
│   └── SourcePlatform.java
├── ratelimit/RateLimiter.java
├── repository/BugReportRepository.java
└── service/
    ├── BugReportService.java
    ├── FileStorageService.java
    └── impl/
        ├── BugReportServiceImpl.java
        └── FileStorageServiceImpl.java
```

## Конфигурация

Основные настройки в `src/main/resources/application.yaml`:
- `spring.datasource.*` — подключение к PostgreSQL
- `app.upload.dir` — директория для скриншотов (по умолчанию `uploads/`)
- `spring.servlet.multipart.max-file-size` — макс. размер файла (10MB)
