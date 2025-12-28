# Identity Manager

System zarządzania użytkownikami z panelem administracyjnym i REST API.

## Technologie

- Java 21
- Spring Boot 3.4.1
- Spring Security 6.4.2
- Spring Data JPA
- H2 Database
- Thymeleaf
- Maven
- JUnit 5 + Mockito
- JaCoCo

## Funkcjonalności

### Backend
- Model danych (User, Role, SupportTicket)
- Repository JPA + JdbcTemplate
- Service layer z DTO
- REST API
- Walidacja danych
- Obsługa wyjątków (GlobalExceptionHandler)

### Frontend
- Panel administratora (Thymeleaf)
- CRUD użytkowników z paginacją
- Import/Export CSV
- Responsywny interfejs

### Bezpieczeństwo
- Spring Security (BCrypt)
- Dual filter chain (API + MVC)
- Role-based authorization (USER, ADMIN)
- CSRF protection

### Testy
- Repository tests (@DataJpaTest) - 12 testów
- Service tests (Mockito) - 20 testów
- Controller tests (MockMvc) - 19 testów
- Pokrycie: 71% instructions, 79% lines

## Uruchomienie
```bash
mvn spring-boot:run
```

## Dostęp

- Panel Admina: http://localhost:8080/admin/users
- API Docs: http://localhost:8080/swagger-ui.html
- H2 Console: http://localhost:8080/h2-console

## Dane testowe

Admin:
- Email: admin@example.com
- Hasło: password123

User:
- Email: john@example.com
- Hasło: password123

## Testy
```bash
mvn clean test
```

## Raport pokrycia
```bash
mvn jacoco:report
```

Raport dostępny w: `target/site/jacoco/index.html`

## Struktura
```
src/main/java/com/example/identitymanager/
├── config/
├── controller/
├── dto/
├── exception/
├── model/
├── repository/
└── service/

src/main/resources/
├── templates/
├── application.yml
└── schema.sql
```

## API Endpoints

### Users
- POST /api/users - Rejestracja
- GET /api/users - Lista użytkowników
- GET /api/users/{id} - Szczegóły użytkownika
- GET /api/users/email/{email} - Użytkownik po email

### Authentication
- GET /api/me - Aktualny użytkownik

### Support Tickets
- GET /api/tickets - Lista zgłoszeń
- POST /api/tickets - Nowe zgłoszenie
- GET /api/tickets/{id} - Szczegóły zgłoszenia
- PATCH /api/tickets/{id}/status - Zmiana statusu

## Baza danych

H2 Console:
- URL: jdbc:h2:mem:identitydb
- Username: sa
- Password: (puste)