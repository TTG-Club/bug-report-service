# Bug Report Service

Микросервис для учёта багов и работы с ними.

## Технологии

- Java 21
- Spring Boot 3.5.0
- PostgreSQL
- Liquibase (миграции БД)
- Spring Security + JWT (внешний auth-сервис)
- AWS SDK v2 (S3-совместимое хранилище для скриншотов)
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
CREATE DATABASE bug;
```

### Переменные окружения

| Переменная | Описание |
|------------|----------|
| `DB_USERNAME` | Имя пользователя PostgreSQL |
| `DB_PASSWORD` | Пароль PostgreSQL |
| `S3_BUCKET` | Имя S3-бакета для скриншотов |
| `S3_ACCESS_KEY` | Access key для S3 |
| `S3_SECRET_KEY` | Secret key для S3 |

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
Authorization: Bearer <token>
```

Требуется роль: `ADMIN` или `MODERATOR`

### Список баг-репортов (с фильтрацией и пагинацией)

```
GET /api/v1/bugs?status=NEW&sourcePlatform=SITE_5E24&page=0&size=20
Authorization: Bearer <token>
```

Требуется роль: `ADMIN` или `MODERATOR`

### Обновление статуса

```
PATCH /api/v1/bugs/{id}/status
Authorization: Bearer <token>
Content-Type: application/json

{
  "status": "FIXED",
  "comment": "Исправлено в релизе 2.1.0"
}
```

Требуется роль: `ADMIN` или `MODERATOR`

Доступные статусы: `NEW`, `FIXED`, `REJECTED`

## Безопасность

- Эндпоинты GET и PATCH защищены JWT-авторизацией
- Валидация токена происходит через внешний сервис `https://auth.api.ttg.club`
- Доступ к защищённым эндпоинтам имеют только пользователи с ролями `ADMIN` или `MODERATOR`
- Эндпоинт создания баг-репорта (POST) доступен без авторизации

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
├── config/
│   ├── S3Config.java
│   └── SecurityConfig.java
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
├── security/
│   ├── AuthValidationResponse.java
│   ├── ExternalAuthClient.java
│   └── JwtAuthenticationFilter.java
└── service/
    ├── BugReportService.java
    ├── FileStorageService.java
    └── impl/
        ├── BugReportServiceImpl.java
        └── FileStorageServiceImpl.java
```

## Конфигурация

Основные настройки в `src/main/resources/application.yaml`:
- `spring.datasource.*` — подключение к PostgreSQL (БД: `bug`)
- `app.auth.*` — настройки внешнего auth-сервиса
- `app.s3.*` — настройки S3-хранилища (Beget Cloud)
- `spring.servlet.multipart.max-file-size` — макс. размер файла (10MB)
