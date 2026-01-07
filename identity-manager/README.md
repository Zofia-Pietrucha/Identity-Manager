# Identity Manager - System Zarządzania Tożsamością

Aplikacja webowa do zarządzania użytkownikami z panelem administracyjnym, REST API i systemem zgłoszeń wsparcia technicznego.

## Technologie

- Java 21
- Spring Boot 3.4.1
- Spring Security 6.4.2
- Spring Data JPA
- H2 Database (in-memory)
- Thymeleaf
- OpenCSV
- Swagger/OpenAPI 3.0
- JUnit 5
- Mockito
- Maven

## Funkcjonalności

### Backend

**REST API**
- Pełny CRUD dla użytkowników i zgłoszeń wsparcia
- Autentykacja HTTP Basic i dedykowany endpoint logowania
- Autoryzacja oparta na rolach (USER, ADMIN)
- Walidacja danych wejściowych (Bean Validation)
- Obsługa błędów z GlobalExceptionHandler
- Paginacja wyników
- Wyszukiwanie i filtrowanie
- Upload i zarządzanie avatarami użytkowników

**Dostęp do danych**
- Spring Data JPA z custom queries (JPQL)
- JdbcTemplate z implementacją CRUD
- Interfejs UserDao demonstrujący różne podejścia do persystencji

**Bezpieczeństwo**
- Dual filter chain - osobna konfiguracja dla API i widoków MVC
- Hashowanie haseł algorytmem BCrypt
- Ochrona CSRF dla widoków HTML
- Stateless API (SessionCreationPolicy.STATELESS)

### Frontend

**Panel Administracyjny (Thymeleaf + Bootstrap 5)**
- Zarządzanie użytkownikami (lista, dodawanie, edycja, usuwanie)
- Paginacja manualna
- Import użytkowników z plików CSV
- Export użytkowników do CSV
- Zarządzanie zgłoszeniami wsparcia
- Zmiana statusu zgłoszeń
- Upload avatarów użytkowników
- Flash messages dla feedbacku użytkownika

**Dashboard Użytkownika**
- Podgląd i edycja własnego profilu
- Zmiana avatara
- Ustawienia prywatności
- Lista własnych zgłoszeń wsparcia

## Endpointy API

### Authentication

```
POST   /api/auth/login              Logowanie (zwraca dane użytkownika)
GET    /api/auth/me                 Pobierz dane zalogowanego użytkownika
PUT    /api/auth/me                 Aktualizuj profil
PATCH  /api/auth/me/privacy         Zmień ustawienia prywatności
```

### Users

```
POST   /api/users                   Rejestracja nowego użytkownika (publiczny)
GET    /api/users                   Lista wszystkich użytkowników
GET    /api/users/paginated         Lista z paginacją
GET    /api/users/search            Wyszukiwanie użytkowników
GET    /api/users/{id}              Pobierz użytkownika po ID
GET    /api/users/email/{email}     Pobierz użytkownika po adresie email
GET    /api/users/by-role/{role}    Filtruj użytkowników po roli
GET    /api/users/stats/privacy     Statystyki ustawień prywatności
PUT    /api/users/{id}              Aktualizuj dane użytkownika
DELETE /api/users/{id}              Usuń użytkownika
```

### User Avatars

```
POST   /api/users/{id}/avatar       Upload avatara
GET    /api/users/{id}/avatar       Pobierz avatar
DELETE /api/users/{id}/avatar       Usuń avatar
```

### Support Tickets

```
GET    /api/tickets                 Lista wszystkich zgłoszeń
POST   /api/tickets                 Utwórz nowe zgłoszenie
GET    /api/tickets/{id}            Pobierz szczegóły zgłoszenia
GET    /api/tickets/user/{userId}   Zgłoszenia konkretnego użytkownika
PATCH  /api/tickets/{id}/status     Zmień status zgłoszenia
```

### Validation Testing

```
POST   /api/validation/test         Test walidacji DTO
```

## Konfiguracja bezpieczeństwa

### Dual Filter Chain

Aplikacja wykorzystuje dwa niezależne łańcuchy filtrów:

**1. API Filter Chain (`/api/**`)**
- Tryb stateless (bez sesji)
- Autentykacja HTTP Basic
- Publiczne endpointy: `/api/users`, `/api/auth/login`
- Pozostałe endpointy wymagają autentykacji

**2. MVC Filter Chain (`/**`)**
- Tryb stateful (sesje)
- Logowanie przez formularz
- Dostęp do `/admin/**` tylko dla użytkowników z rolą ADMIN
- Publiczne: `/login`, `/h2-console`, `/swagger-ui/**`

### Hasła

Wszystkie hasła są hashowane przy użyciu BCrypt (strength 10).
Domyślne hasło dla użytkowników testowych: `password123`

## Baza danych

### Konfiguracja H2

H2 Console dostępna pod adresem: `http://localhost:8080/h2-console`

```
JDBC URL:  jdbc:h2:mem:identitydb
Username:  sa
Password:  (puste)
```

### Dane testowe

Aplikacja automatycznie inicjalizuje bazę następującymi danymi:

**Użytkownicy:**
- admin@example.com / password123 (rola: ADMIN)
- john@example.com / password123 (rola: USER)
- jane@example.com / password123 (rola: USER)

**Zgłoszenia wsparcia:**
- 3 przykładowe zgłoszenia z różnymi statusami

## Instalacja i uruchomienie

### Wymagania

- Java 21 lub nowsza
- Maven 3.6+

### Uruchomienie aplikacji

```bash
git clone <repository-url>
cd identity-manager

mvn clean install
mvn spring-boot:run
```

Aplikacja będzie dostępna pod adresem: `http://localhost:8080`

### Dostęp do interfejsów

- Panel administracyjny: `http://localhost:8080/admin/users`
- Dashboard użytkownika: `http://localhost:8080/user/dashboard`
- Dokumentacja API (Swagger UI): `http://localhost:8080/swagger-ui.html`
- Konsola H2: `http://localhost:8080/h2-console`

## Testy

### Uruchomienie wszystkich testów

```bash
mvn test
```

### Uruchomienie testów konkretnej warstwy

```bash
mvn test -Dtest=*RepositoryTest
mvn test -Dtest=*ServiceTest
mvn test -Dtest=*ControllerTest
```

### Raport pokrycia kodu (JaCoCo)

```bash
mvn jacoco:report
```

Raport dostępny w: `target/site/jacoco/index.html`

### Pokrycie testów

Aktualne pokrycie kodu: **~90%**

- Repository layer: testy integracyjne z `@DataJpaTest`
- Service layer: testy jednostkowe z Mockito
- Controller layer: testy z MockMvc
- DTO/Model/Exception: testy jednostkowe

## Struktura projektu

```
src/main/java/com/example/identitymanager/
├── config/
│   ├── SecurityConfig.java
│   ├── PasswordConfig.java
│   └── DataInitializer.java
├── controller/
│   ├── AuthController.java
│   ├── UserController.java
│   ├── UserDashboardController.java
│   ├── SupportTicketController.java
│   ├── AdminController.java
│   ├── AdminTicketController.java
│   ├── WebController.java
│   └── ValidationTestController.java
├── dto/
│   ├── UserDTO.java
│   ├── UserRegistrationDTO.java
│   ├── UserUpdateDTO.java
│   ├── LoginRequest.java
│   ├── LoginResponse.java
│   ├── SupportTicketDTO.java
│   ├── CreateTicketRequest.java
│   └── UpdateTicketStatusRequest.java
├── exception/
│   ├── ResourceNotFoundException.java
│   ├── DuplicateResourceException.java
│   ├── ErrorResponse.java
│   └── GlobalExceptionHandler.java
├── model/
│   ├── User.java
│   ├── Role.java
│   └── SupportTicket.java
├── repository/
│   ├── UserRepository.java
│   ├── RoleRepository.java
│   ├── SupportTicketRepository.java
│   ├── UserDao.java
│   └── JdbcUserDao.java
├── service/
│   ├── UserService.java
│   ├── CustomUserDetailsService.java
│   ├── FileStorageService.java
│   └── SupportTicketService.java
└── IdentityManagerApplication.java

src/main/resources/
├── templates/
│   ├── admin/
│   │   ├── users-list.html
│   │   ├── user-form.html
│   │   ├── import-csv.html
│   │   ├── tickets-list.html
│   │   └── ticket-detail.html
│   ├── login.html
│   ├── dashboard.html
│   └── 403.html
├── application.yml
└── schema.sql

src/test/java/com/example/identitymanager/
├── controller/
├── service/
├── repository/
├── model/
├── dto/
└── exception/
```

## Kluczowe implementacje

### Custom JPQL Query z Pageable

```java
@Query("SELECT u FROM User u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);
```

### JdbcTemplate CRUD

```java
@Override
public int insertUser(User user) {
    String sql = "INSERT INTO users (email, password, first_name, last_name, phone, " +
                 "is_privacy_enabled, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    
    KeyHolder keyHolder = new GeneratedKeyHolder();
    
    jdbcTemplate.update(connection -> {
        PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
        ps.setString(1, user.getEmail());
        // ... pozostałe parametry
        return ps;
    }, keyHolder);
    
    return rowsAffected;
}
```

### Bean Validation

```java
@NotBlank(message = "Email is required")
@Email(message = "Email must be valid")
private String email;

@NotBlank(message = "Password is required")
@Size(min = 8, message = "Password must be at least 8 characters")
private String password;
```

### OpenCSV Import

```java
try (CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
    List<String[]> allRows = csvReader.readAll();
    
    for (int i = 1; i < allRows.size(); i++) {
        String[] fields = allRows.get(i);
        // przetwarzanie danych...
    }
}
```

### Global Exception Handler

```java
@ExceptionHandler(ResourceNotFoundException.class)
public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
    ErrorResponse error = new ErrorResponse(
        LocalDateTime.now(),
        HttpStatus.NOT_FOUND.value(),
        "Not Found",
        ex.getMessage()
    );
    return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
}
```

## Przykłady użycia API

### Logowanie

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "password123"
  }'
```

### Pobranie listy użytkowników

```bash
curl -u john@example.com:password123 \
  http://localhost:8080/api/users
```

### Utworzenie zgłoszenia wsparcia

```bash
curl -u john@example.com:password123 \
  -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 2,
    "subject": "Problem z aplikacją",
    "description": "Szczegółowy opis problemu"
  }'
```

### Aktualizacja profilu

```bash
curl -u john@example.com:password123 \
  -X PUT http://localhost:8080/api/auth/me \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jonathan",
    "lastName": "Smith",
    "phone": "+48123456789"
  }'
```

### Upload avatara

```bash
curl -u john@example.com:password123 \
  -X POST http://localhost:8080/api/users/2/avatar \
  -F "file=@avatar.jpg"
```

### Zmiana ustawień prywatności

```bash
curl -u john@example.com:password123 \
  -X PATCH http://localhost:8080/api/auth/me/privacy \
  -H "Content-Type: application/json" \
  -d '{
    "isPrivacyEnabled": true
  }'
```

## Import/Export CSV

### Export użytkowników

Przejdź do panelu administracyjnego i kliknij "Export to CSV" lub wykonaj request:

```
GET http://localhost:8080/admin/users/export
```

Zostanie pobrany plik `users_export.csv` z danymi wszystkich użytkowników.

### Import użytkowników

1. Przygotuj plik CSV w formacie:
   ```
   email,firstName,lastName,phone,isPrivacyEnabled
   user@example.com,Jan,Kowalski,+48123456789,false
   ```

2. W panelu admina przejdź do `/admin/users/import`

3. Wybierz plik i kliknij "Import"

Użytkownicy zostaną dodani z domyślnym hasłem `password123`.

## Dokumentacja API

Pełna interaktywna dokumentacja API dostępna w Swagger UI:

```
http://localhost:8080/swagger-ui.html
```

Dokumentacja zawiera opisy wszystkich endpointów, parametrów, schematów DTO oraz możliwość testowania API bezpośrednio z przeglądarki.

## Wymagania spełnione

- Model danych z relacjami (User, Role, SupportTicket)
- Repository JPA z custom queries
- JdbcTemplate z pełną implementacją CRUD
- Service layer z logiką biznesową
- DTO pattern dla separacji warstw
- REST API z walidacją
- Global Exception Handler
- Spring Security z BCrypt
- Panel administracyjny (Thymeleaf)
- Dashboard użytkownika
- Upload avatarów (FileStorageService)
- Import/Export CSV (OpenCSV)
- Paginacja (JPA Pageable)
- Testy jednostkowe i integracyjne (~90% coverage)
- Dokumentacja API (Swagger)