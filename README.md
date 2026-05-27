# Bug Report Service

Микросервис для приема, хранения и модерации баг-репортов.

## Технологии

- Java 21
- Spring Boot 3.5.0
- PostgreSQL
- Liquibase
- Spring Security + JWT
- SpringDoc OpenAPI / Swagger UI
- AWS SDK v2 для S3-совместимого хранилища
- Bucket4j для rate limiting
- MapStruct
- Lombok

## Требования

- JDK 21+
- PostgreSQL 14+
- Maven 3.9+ или встроенный `mvnw.cmd`

## Настройка

Создайте базу данных:

```sql
CREATE DATABASE bug;
```

Минимальный набор переменных окружения:

| Переменная | Описание |
| --- | --- |
| `SPRING_DATASOURCE_URL` | JDBC URL PostgreSQL, например `jdbc:postgresql://localhost:5432/bug` |
| `SPRING_DATASOURCE_USERNAME` | Имя пользователя PostgreSQL |
| `SPRING_DATASOURCE_PASSWORD` | Пароль PostgreSQL |
| `JWT_SECRET` | Секрет для проверки JWT |
| `SPRING_CLOUD_AWS_S3_ENDPOINT` | Endpoint S3-совместимого хранилища |
| `SPRING_CLOUD_AWS_S3_REGION` | Регион S3 |
| `SPRING_CLOUD_AWS_S3_BUCKET` | Имя bucket для скриншотов |
| `SPRING_CLOUD_AWS_CREDENTIALS_ACCESS_KEY` | Access key для S3 |
| `SPRING_CLOUD_AWS_CREDENTIALS_SECRET_KEY` | Secret key для S3 |

Основные настройки лежат в [application.yaml](src/main/resources/application.yaml).

## Запуск

```bash
./mvnw spring-boot:run
```

Для Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Приложение запускается на порту `8080`.

Swagger UI доступен по адресу:

```text
http://localhost:8080/swagger-ui/index.html
```

## Сборка

```bash
./mvnw test
./mvnw package
```

Для Windows:

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
```

## Docker

```bash
docker build -t bug-report-service .
docker run --rm -p 8080:8080 --env-file .env bug-report-service
```

## API

### Создание баг-репорта

```http
POST /api/v1/bugs
Content-Type: multipart/form-data
```

Части multipart-запроса:

| Поле | Обязательное | Описание |
| --- | --- | --- |
| `request` | да | JSON с данными баг-репорта |
| `screenshot` | нет | Скриншот бага, максимум `10MB` |

JSON в поле `request`:

```json
{
  "description": "Кнопка 'Сохранить' не работает",
  "url": "https://ttg.club/characters/123",
  "sourcePlatform": "SITE_5E24",
  "sessionId": "sess_abc123def456"
}
```

Ограничения:

- `description` обязателен, максимум `2000` символов.
- `url` опционален, максимум `1000` символов.
- `sourcePlatform` обязателен.
- Для авторизованных пользователей логин берется из JWT (`username` или `sub`).
- `sessionId` используется для неавторизованных пользователей.

### Получение баг-репорта по ID

```http
GET /api/v1/bugs/{id}
Authorization: Bearer <token>
```

Требуется роль `ADMIN` или `MODERATOR`.

### Список баг-репортов

```http
GET /api/v1/bugs?status=NEW&sourcePlatform=SITE_5E24&page=0&size=20
Authorization: Bearer <token>
```

Поддерживаются фильтры:

- `status`
- `sourcePlatform`
- стандартные параметры пагинации Spring Data: `page`, `size`, `sort`

Требуется роль `ADMIN` или `MODERATOR`.

### Обновление статуса

```http
PATCH /api/v1/bugs/{id}/status
Authorization: Bearer <token>
Content-Type: application/json

{
  "status": "FIXED",
  "comment": "Исправлено в релизе 2.1.0"
}
```

Требуется роль `ADMIN` или `MODERATOR`.

### Список статусов

```http
GET /api/v1/bugs/statuses
```

Эндпоинт доступен без авторизации.

## Справочники

Доступные статусы:

| Enum | Название |
| --- | --- |
| `NEW` | новый |
| `WAIT` | ожидает |
| `FIXED` | исправлено |
| `REJECTED` | отклонено |

Платформы-источники:

| Enum | Описание |
| --- | --- |
| `SITE_5E24` | Сайт 5e24 |
| `SITE_5E14` | Сайт 5e14 |
| `VTTG` | VTTG |

## Безопасность

- `POST /api/v1/bugs` и `GET /api/v1/bugs/statuses` доступны без авторизации.
- `GET /api/v1/bugs`, `GET /api/v1/bugs/{id}` и `PATCH /api/v1/bugs/{id}/status` защищены JWT.
- Для защищенных эндпоинтов нужна роль `ADMIN` или `MODERATOR`.
- JWT проверяется локально через `JwtDecoder`.
- Имя пользователя берется из claim `username` или из `sub`.
- Роли берутся из claim `roles` или `authorities`; префикс `ROLE_` поддерживается.

## Rate Limiting

| Тип пользователя | Лимит |
| --- | --- |
| Авторизованный, по логину из JWT | 1 баг в 10 секунд |
| Неавторизованный, по `sessionId` | 1 баг в минуту |

## Структура проекта

```text
src/main/java/club/ttg/bug/report/
├── BugReportServiceApplication.java
├── config/
│   ├── JwtConfig.java
│   ├── OpenApiConfig.java
│   ├── S3Config.java
│   └── SecurityConfig.java
├── controller/
│   └── BugReportController.java
├── dto/
│   ├── BugReportCreateRequest.java
│   ├── BugReportResponse.java
│   ├── BugReportUpdateStatusRequest.java
│   └── BugStatusResponse.java
├── exception/
│   ├── BugReportNotFoundException.java
│   ├── FileStorageException.java
│   ├── GlobalExceptionHandler.java
│   └── RateLimitExceededException.java
├── mapper/
│   └── BugReportMapper.java
├── model/
│   ├── BugReport.java
│   ├── BugStatus.java
│   └── SourcePlatform.java
├── ratelimit/
│   └── RateLimiter.java
├── repository/
│   └── BugReportRepository.java
├── security/
│   ├── JwtAuthenticatedUser.java
│   ├── JwtAuthenticationFilter.java
│   └── JwtTokenValidator.java
└── service/
    ├── BugReportService.java
    ├── FileStorageService.java
    └── impl/
        ├── BugReportServiceImpl.java
        └── FileStorageServiceImpl.java
```

## Миграции

Liquibase changelog находится в `src/main/resources/db/changelog/db.changelog-master.yaml`.
