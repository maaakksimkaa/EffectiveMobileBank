# Effective Mobile Bank

Backend-приложение для управления банковскими картами на Java с использованием Spring Boot.

## Описание

Приложение предоставляет REST API для:
- Аутентификации и авторизации пользователей
- Создания и управления банковскими картами
- Переводов между картами
- Управления пользователями (для администраторов)
- Безопасного хранения данных с шифрованием

## Технологии

- **Java 21**
- **Spring Boot 3.5.4**
- **Spring Security + JWT**
- **Spring Data JPA**
- **PostgreSQL**
- **Liquibase** (миграции БД)
- **Docker & Docker Compose**
- **Swagger/OpenAPI 3**
- **Maven**

## Требования

- **Java 21** или выше
- **Docker** и **Docker Compose**
- **Maven 3.6** или выше
- **Git**

## Быстрый запуск

### Вариант 1: Запуск через Docker Compose (Рекомендуется)

#### 1. Клонирование репозитория
```bash
git clone <repository-url>
cd EffectiveMobileBank
```

#### 2. Запуск базы данных
```bash
docker-compose up -d postgres
```

#### 3. Запуск приложения
```bash
./mvnw spring-boot:run
```

#### 4. Проверка работы
- Приложение: http://localhost:9090
- Swagger UI: http://localhost:9090/swagger-ui
- API Docs: http://localhost:9090/api/docs

### Вариант 2: Запуск через Docker

#### 1. Сборка и запуск
```bash
# Сборка образа
docker build -t effective-mobile-bank .

# Запуск с базой данных
docker-compose up -d
```

#### 2. Проверка работы
- Приложение: http://localhost:9090
- Swagger UI: http://localhost:9090/swagger-ui

### Вариант 3: Локальный запуск

#### 1. Настройка базы данных
```bash
# Запуск PostgreSQL через Docker
docker run --name postgres-bank \
  -e POSTGRES_DB=effective_bank \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=1234 \
  -p 5432:5432 \
  -d postgres:15
```

#### 2. Запуск приложения
```bash
./mvnw spring-boot:run
```

## Конфигурация

### Основные настройки (`application.properties`)

```properties
# Сервер
server.port=9090
spring.application.name=EffectiveMobileBank

# База данных
spring.datasource.url=jdbc:postgresql://localhost:5432/effective_bank
spring.datasource.username=postgres
spring.datasource.password=1234

# JWT
security.jwt.secret=secret-secret-secret-in-key
security.jwt.expiration-minutes=60

# Swagger
springdoc.api-docs.path=/api/docs
springdoc.swagger-ui.path=/swagger-ui
```

### Переменные окружения

```bash
# Настройка JWT секрета
export JWT_SECRET=your-secret-key-here

# Настройка порта
export SERVER_PORT=9090

# Настройка базы данных
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=effective_bank
export DB_USER=postgres
export DB_PASSWORD=1234
```

## API Endpoints

### Аутентификация
| Метод | Endpoint | Описание |
|-------|----------|----------|
| `POST` | `/api/auth/register` | Регистрация пользователя |
| `POST` | `/api/auth/login` | Вход в систему |

### Пользовательские операции
| Метод | Endpoint | Описание |
|-------|----------|----------|
| `GET` | `/api/cards` | Получение списка своих карт |
| `POST` | `/api/cards/{id}/block` | Блокировка карты |
| `POST` | `/api/cards/transfer` | Перевод между своими картами |
| `POST` | `/api/cards/{id}/topup` | Пополнение карты |

### Административные операции
| Метод | Endpoint | Описание |
|-------|----------|----------|
| `POST` | `/api/admin/cards` | Создание карты для пользователя |
| `GET` | `/api/admin/cards` | Получение списка всех карт |
| `DELETE` | `/api/admin/cards/{id}` | Удаление карты |
| `POST` | `/api/admin/cards/{id}/topup` | Пополнение любой карты |
| `POST` | `/api/admin/users` | Создание пользователя |
| `GET` | `/api/admin/users` | Получение списка пользователей |
| `PATCH` | `/api/admin/users/{id}/roles` | Обновление ролей пользователя |
| `DELETE` | `/api/admin/users/{id}` | Удаление пользователя |

## Тестовые данные

При запуске автоматически создаются тестовые пользователи:

### Администратор
- **Логин**: `admin`
- **Пароль**: `admin123`
- **Роли**: `ADMIN`, `USER`

### Пользователь
- **Логин**: `user`
- **Пароль**: `user123`
- **Роли**: `USER`

## Безопасность

- **JWT токены** для аутентификации
- **Ролевая авторизация** (ADMIN, USER)
- **Шифрование номеров карт** (AES/GCM)
- **Маскирование номеров карт** в ответах
- **Валидация входных данных**
- **Защита от SQL-инъекций**

## База данных

### Автоматическая инициализация
База данных автоматически инициализируется через **Liquibase** при запуске приложения.

### Структура таблиц
- `users` - пользователи системы
- `user_roles` - роли пользователей
- `cards` - банковские карты

## Тестирование

### Запуск тестов
```bash
# Все тесты
./mvnw test

# Конкретный тест
./mvnw test -Dtest=AuthControllerTest

# Тесты с отчетом
./mvnw test jacoco:report
```

### Покрытие тестами
- **Unit тесты** для сервисов
- **Integration тесты** для контроллеров
- **Тесты обработки ошибок**

## Разработка

### Сборка проекта
```bash
# Очистка и сборка
./mvnw clean package

# Сборка без тестов
./mvnw clean package -DskipTests
```

### Запуск в режиме разработки
```bash
# С профилем dev
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# С отладкой
./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

### Структура проекта
```
src/main/java/bank/effectivemobilebank/
├── config/          # Конфигурации
│   ├── ControllerAdviceConfig.java  # Обработка ошибок
│   ├── SecurityConfig.java          # Безопасность
│   └── ...
├── controller/      # REST контроллеры
├── dto/            # Data Transfer Objects
├── mapper/         # Мапперы
├── model/          # JPA сущности
├── repository/     # Репозитории
├── security/       # JWT и безопасность
└── service/        # Бизнес-логика
```

## Обработка ошибок

Приложение включает комплексную обработку ошибок:

### Типы обрабатываемых ошибок
- **400 Bad Request** - некорректные параметры
- **401 Unauthorized** - неверные учетные данные
- **403 Forbidden** - недостаточно прав
- **404 Not Found** - ресурс не найден
- **500 Internal Server Error** - внутренние ошибки

### Примеры ответов об ошибках
```json
{
  "error": "Пользователь уже существует"
}
```

```json
{
  "fromCardId": "ID карты-отправителя обязателен",
  "amount": "Сумма должна быть больше 0"
}
```


## Развертывание

### Docker Compose (Production)
```bash
# Сборка и запуск
docker-compose -f docker-compose.prod.yml up -d

# Просмотр логов
docker-compose logs -f app
```

