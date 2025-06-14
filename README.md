# Studentify Server - Документация

![Ktor + PostgreSQL](https://img.shields.io/badge/Ktor-PostgreSQL-blue?logo=postgresql&logoColor=white)

## Ссылки
- **Клиентское Android-приложение**: [StudentService на GitHub](https://github.com/mareliberum/StudentService)
- **Ktor Framework**: [Официальная документация](https://ktor.io/)
- **Exposed ORM**: [GitHub репозиторий](https://github.com/JetBrains/Exposed)
- **PostgreSQL**: [Официальная документация](https://www.postgresql.org/docs/)

## Технологический стек
- **Язык**: Kotlin 1.9.0
- **Фреймворк**: Ktor 2.3.5
- **База данных**: PostgreSQL 15+
- **ORM**: Exposed 0.61.0
- **Аутентификация**: JWT + BCrypt
- **Логирование**: Logback
- **Сериализация**: kotlinx.serialization

## Архитектура сервера
Сервер реализован по принципу чистой архитектуры с разделением на слои:

```
Android Client → Ktor Server (REST API)
                 ├── Auth Layer (JWT)
                 ├── Business Logic
                 └── PostgreSQL Database
```

## Ключевые особенности
1. Ролевая модель (Студенты/Преподаватели)
2. JWT-аутентификация
3. Реализация внутренней "валюты" (токены)
4. CRUD операции для управления студентами
5. Автоматическая валидация данных
6. Поддержка WebSockets для real-time обновлений

## Установка и запуск

### Требования
- Java 17+
- PostgreSQL 15+
- Порт 8080 открыт для входящих подключений

### Настройка базы данных
1. Создайте базу данных:
```sql
CREATE DATABASE studentify;
```

2. Создайте таблицы:
```sql
CREATE TABLE students (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    age INTEGER NOT NULL CHECK,
    group_name VARCHAR(50) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    tokens INTEGER NOT NULL
);

CREATE TABLE teachers (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    age INTEGER NOT NULL CHECK,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL
);
```

### Конфигурация сервера
Создайте файл `application.yaml` в директории `resources`:
```yaml
ktor:
  application:
    modules: [com.studentify.ApplicationKt.module]
  deployment:
    port: 8080
    host: 0.0.0.0

postgres:
  url: "jdbc:postgresql://localhost:5432/studentify"
  user: "your_username"
  password: "your_password"

jwt:
  secret: "your_strong_secret_here_at_least_32_chars"
  issuer: "studentify-server"
  audience: "studentify-app"
  realm: "Studentify API"
```

### Запуск сервера
```bash
./gradlew build
java -jar build/libs/studentify-server.jar
```

## API Endpoints

### Без аутентификации
| Метод | Endpoint          | Описание                  |
|-------|-------------------|---------------------------|
| GET   | `/`               | Статус сервера            |
| POST  | `/auth/register`  | Регистрация пользователя  |
| POST  | `/auth/login`     | Авторизация               |

### Для студентов
| Метод | Endpoint                  | Описание                     |
|-------|---------------------------|------------------------------|
| GET   | `/students`               | Список всех студентов        |
| GET   | `/students/count`         | Количество студентов         |
| GET   | `/students/name/{name}`   | Поиск студента по имени      |
| GET   | `/students/group/{group}` | Студенты по группе           |
| GET   | `/student/me`             | Данные текущего студента     |

### Для преподавателей
| Метод | Endpoint                | Описание                     |
|-------|-------------------------|------------------------------|
| GET   | `/teacher/me`           | Данные текущего преподавателя|
| POST  | `/students`             | Добавить нового студента     |
| PUT   | `/students/{id}`        | Обновить данные студента     |
| DELETE| `/students/{id}`        | Удалить студента             |

## Как обратиться к серверу с другого устройства

### Шаг 1: Получите доступ к серверу
1. **Если сервер запущен локально**:
   - Узнайте ваш публичный IP: [whatismyip.com](https://whatismyip.com)
   - Настройте проброс портов на роутере:
     - Внешний порт: 8080
     - Внутренний IP: [IP вашего компьютера]
     - Внутренний порт: 8080
   - Или используйте ngrok для временного туннеля:
     ```bash
     ngrok http 8080
     ```
   - При разработке использовался BaseUrl адрес (и автор (я) может его открыть для теста)
     ```shell
      https://modern-marmot-eminently.ngrok-free.app/
     ```

2. **Если сервер на VPS**:
   - Убедитесь, что порт 8080 открыт в брандмауэре:
     ```bash
     sudo ufw allow 8080/tcp
     ```

### Шаг 2: Тестирование с помощью cURL

1. **Регистрация студента**:
```bash
curl -X POST http://<SERVER_IP>:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Иван Иванов",
    "age": 20,
    "email": "ivanov@edu.ru",
    "password": "securePass123",
    "role": "STUDENT",
    "group": "CS-101"
  }'
```

2. **Авторизация**:
```bash
curl -X POST http://<SERVER_IP>:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "ivanov@edu.ru", "password": "securePass123"}'
```

3. **Доступ к защищённому ресурсу** (используйте токен из предыдущего ответа):
```bash
curl http://<SERVER_IP>:8080/student/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

## Примеры запросов и ответов

### Успешная авторизация (200 OK)
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "role": "STUDENT"
}
```

### Получение данных студента (200 OK)
```json
{
  "id": 1,
  "name": "Иван Иванов",
  "age": 20,
  "group": "CS-101",
  "email": "ivanov@edu.ru",
  "password": "hashed_password",
  "tokens": 150
}
```

### Ошибка доступа (403 Forbidden)
```json
{
  "error": "Only teachers can perform this action"
}
```

## Безопасность
1. Все пароли хранятся в виде BCrypt-хешей
2. Используются JWT-токены с 7-дневным сроком действия
3. Автоматическая валидация входных данных
4. Разделение прав доступа по ролям
