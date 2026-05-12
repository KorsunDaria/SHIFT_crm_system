# CRM System

Упрощённая CRM-система для управления продавцами и их транзакциями с аналитикой.

## Структура проекта

```
src/main/java/com/crm/
├── CrmApplication.java
├── config/          — конфигурация Swagger
├── controller/      — REST-контроллеры
├── dto/             — объекты передачи данных
├── entity/          — JPA-сущности
├── enums/           — PaymentType
├── exception/       — обработка ошибок
├── mapper/          — маппинг entity ↔ DTO
├── repository/      — Spring Data репозитории
└── service/         — бизнес-логика
```

## Запуск

### 1. Создать БД в PostgreSQL

```sql
psql postgres -c "CREATE DATABASE crm_db;"
```


### 2. Собрать и запустить

```bash
mvn spring-boot:run
```

Или собрать jar и запустить:

```bash
mvn clean package
java -jar target/crm-system-1.0.0.jar
```

Запустить только тесты:

```bash
mvn test
```

Приложение запустится на `http://localhost:8080`.

Swagger UI доступен по адресу: `http://localhost:8080/swagger-ui.html`

---

## API

### Sellers — `/api/v1/sellers`

| Метод  | Путь                    | Описание                        |
|--------|-------------------------|---------------------------------|
| GET    | `/`                     | Список всех продавцов           |
| GET    | `/{id}`                 | Информация о продавце           |
| POST   | `/`                     | Создать продавца                |
| PUT    | `/{id}`                 | Обновить продавца               |
| DELETE | `/{id}`                 | Удалить продавца (soft delete)  |
| GET    | `/{id}/transactions`    | Транзакции продавца             |

### Transactions — `/api/v1/transactions`

| Метод | Путь    | Описание                    |
|-------|---------|-----------------------------|
| GET   | `/`     | Список всех транзакций      |
| GET   | `/{id}` | Информация о транзакции     |
| POST  | `/`     | Создать транзакцию          |

### Analytics — `/api/v1/analytics`

| Метод | Путь                        | Описание                                   |
|-------|-----------------------------|--------------------------------------------|
| GET   | `/best-seller?period=MONTH` | Самый продуктивный продавец за период      |
| GET   | `/low-performers?from=...&to=...&amount=...` | Продавцы с суммой ниже порога |
| GET   | `/best-period/{sellerId}`   | Лучший период активности продавца          |

Параметр `period`: `DAY`, `MONTH`, `QUARTER`, `YEAR`

---

## Примеры запросов

### Создать продавца
```http
POST /api/v1/sellers
Content-Type: application/json

{
  "name": "Иван Иванов",
  "contactInfo": "ivan@example.com"
}
```

### Создать транзакцию
```http
POST /api/v1/transactions
Content-Type: application/json

{
  "sellerId": 1,
  "amount": 1500.00,
  "paymentType": "CARD"
}
```

### Лучший продавец за месяц
```http
GET /api/v1/analytics/best-seller?period=MONTH
```

### Продавцы с суммой ниже 5000 за период
```http
GET /api/v1/analytics/low-performers?from=2025-01-01T00:00:00&to=2024-12-31T23:59:59&amount=5000
```

### Лучший период продавца
```http
GET /api/v1/analytics/best-period/1
```

---

