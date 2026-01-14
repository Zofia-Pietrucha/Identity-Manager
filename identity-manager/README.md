# Identity Manager - System Zarzadzania Tozsamoscia

Aplikacja webowa do zarzadzania uzytkownikami z panelem administracyjnym, REST API i systemem zgloszen wsparcia technicznego.

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

## Funkcjonalnosci

### Backend

**REST API**
- Pelny CRUD dla uzytkownikow i zgloszen wsparcia
- Autentykacja HTTP Basic i dedykowany endpoint logowania
- Autoryzacja oparta na rolach (USER, ADMIN)
- Walidacja danych wejsciowych (Bean Validation + custom validators)
- Obsluga bledow z GlobalExceptionHandler
- Paginacja wynikow
- Wyszukiwanie i filtrowanie
- Upload i zarzadzanie avatarami uzytkownikow

**Dostep do danych**
- Spring Data JPA z custom queries (JPQL z JOIN)
- JdbcTemplate z pelna implementacja CRUD
- Interfejs UserDao demonstrujacy rozne podejscia do persystencji

**Bezpieczenstwo**
- Dual filter chain - osobna konfiguracja dla API i widokow MVC
- Hashowanie hasel algorytmem BCrypt
- Ochrona CSRF dla widokow HTML
- Stateless API (SessionCreationPolicy.STATELESS)
- Kontrola dostepu do ticketow - USER widzi tylko swoje, ADMIN wszystkie

### Frontend

**Panel Administracyjny (Thymeleaf + Bootstrap 5)**
- Zarzadzanie uzytkownikami (lista, dodawanie, edycja, usuwanie)
- Paginacja manualna
- Import uzytkownikow z plikow CSV
- Export uzytkownikow do CSV
- Zarzadzanie zgloszeniami wsparcia (lista, tworzenie, usuwanie)
- Tworzenie ticketow na dowolnego uzytkownika
- Zmiana statusu zgloszen
- Upload awatarow uzytkownikow
- Flash messages dla feedbacku uzytkownika

**Dashboard Uzytkownika**
- Podglad i edycja wlasnego profilu
- Zmiana avatara
- Ustawienia prywatnosci
- Lista wlasnych zgloszen wsparcia
- Tworzenie nowych zgloszen wsparcia

## Endpointy API

### Authentication
```
POST   /api/auth/login              Logowanie (zwraca dane uzytkownika)
GET    /api/auth/me                 Pobierz dane zalogowanego uzytkownika
PUT    /api/auth/me                 Aktualizuj profil
PATCH  /api/auth/me/privacy         Zmien ustawienia prywatnosci
```

### Users
```
POST   /api/users                   Rejestracja nowego uzytkownika (publiczny)
GET    /api/users                   Lista wszystkich uzytkownikow
GET    /api/users/paginated         Lista z paginacja
GET    /api/users/search            Wyszukiwanie uzytkownikow
GET    /api/users/{id}              Pobierz uzytkownika po ID
GET    /api/users/email/{email}     Pobierz uzytkownika po adresie email
GET    /api/users/by-role/{role}    Filtruj uzytkownikow po roli
GET    /api/users/stats/privacy     Statystyki ustawien prywatnosci
PUT    /api/users/{id}              Aktualizuj dane uzytkownika
DELETE /api/users/{id}              Usun uzytkownika
```

### User Avatars
```
POST   /api/users/{id}/avatar       Upload avatara
GET    /api/users/{id}/avatar       Pobierz avatar
DELETE /api/users/{id}/avatar       Usun avatar
```

### Support Tickets
```
GET    /api/tickets                 Lista zgloszen (USER: tylko swoje, ADMIN: wszystkie)
GET    /api/tickets/my              Lista zgloszen zalogowanego uzytkownika
GET    /api/tickets/{id}            Pobierz szczegoly zgloszenia (USER: tylko swoje)
GET    /api/tickets/user/{userId}   Zgloszenia konkretnego uzytkownika (tylko ADMIN)
POST   /api/tickets                 Utworz zgloszenie (automatycznie dla zalogowanego usera)
POST   /api/tickets/admin           Utworz zgloszenie dla dowolnego usera (tylko ADMIN)
PATCH  /api/tickets/{id}/status     Zmien status zgloszenia (tylko ADMIN)
```

### Validation Testing
```
POST   /api/validation/test         Test walidacji DTO
```

## Uprawnienia do ticketow

| Operacja | USER | ADMIN |
|----------|------|-------|
| Zobacz swoje tickety | ✓ | ✓ |
| Zobacz wszystkie tickety | ✗ | ✓ |
| Utworz ticket na siebie | ✓ | ✓ |
| Utworz ticket na kogos | ✗ | ✓ |
| Zmien status ticketu | ✗ | ✓ |
| Usun ticket | ✗ | ✓ |

## Konfiguracja bezpieczenstwa

### Dual Filter Chain

Aplikacja wykorzystuje dwa niezalezne lancuchy filtrow:

**1. API Filter Chain (`/api/**`)**
- Tryb stateless (bez sesji)
- Autentykacja HTTP Basic
- Publiczne endpointy: `/api/users`, `/api/auth/login`
- Pozostale endpointy wymagaja autentykacji

**2. MVC Filter Chain (`/**`)**
- Tryb stateful (sesje)
- Logowanie przez formularz
- Dostep do `/admin/**` tylko dla uzytkownikow z rola ADMIN
- Publiczne: `/login`, `/h2-console`, `/swagger-ui/**`

### Hasla

Wszystkie hasla sa hashowane przy uzyciu BCrypt (strength 10).
Domyslne haslo dla uzytkownikow testowych: `password123`

## Baza danych

### Konfiguracja H2

H2 Console dostepna pod adresem: `http://localhost:8080/h2-console`
```
JDBC URL:  jdbc:h2:mem:identitydb
Username:  sa
Password:  (puste)
```

### Dane testowe

Aplikacja automatycznie inicjalizuje baze nastepujacymi danymi:

**Uzytkownicy:**
- admin@example.com / password123 (rola: ADMIN)
- john@example.com / password123 (rola: USER)
- jane@example.com / password123 (rola: USER)

**Zgloszenia wsparcia:**
- 3 przykladowe zgloszenia z roznymi statusami

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

Aplikacja bedzie dostepna pod adresem: `http://localhost:8080`

### Dostep do interfejsow

- Panel administracyjny: `http://localhost:8080/admin/users`
- Zarzadzanie ticketami (admin): `http://localhost:8080/admin/tickets`
- Dashboard uzytkownika: `http://localhost:8080/user/dashboard`
- Dokumentacja API (Swagger UI): `http://localhost:8080/swagger-ui.html`
- Konsola H2: `http://localhost:8080/h2-console`

## Testy

### Uruchomienie wszystkich testow
```bash
mvn test
```

### Uruchomienie testow konkretnej warstwy
```bash
mvn test -Dtest=*RepositoryTest
mvn test -Dtest=*ServiceTest
mvn test -Dtest=*ControllerTest
```

### Raport pokrycia kodu (JaCoCo)
```bash
mvn jacoco:report
```

Raport dostepny w: `target/site/jacoco/index.html`

### Pokrycie testow

Aktualne pokrycie kodu: **~89%**

- Repository layer: testy integracyjne z `@DataJpaTest`
- Service layer: testy jednostkowe z Mockito
- Controller layer: testy z MockMvc
- Validation layer: testy custom validators
- DTO/Model/Exception: testy jednostkowe

## Struktura projektu
```
src/main/java/com/example/identitymanager/
├── config/
│   ├── SecurityConfig.java
│   ├── OpenApiConfig.java
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
│   ├── CreateTicketRequestUser.java
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
├── validation/
│   ├── ValidPhone.java
│   ├── PhoneValidator.java
│   ├── ValidName.java
│   └── NameValidator.java
└── IdentityManagerApplication.java

src/main/resources/
├── templates/
│   ├── admin/
│   │   ├── users-list.html
│   │   ├── user-form.html
│   │   ├── import-csv.html
│   │   ├── tickets-list.html
│   │   └── ticket-detail.html
│   ├── user/
│   │   └── dashboard.html
│   ├── login.html
│   └── 403.html
├── application.yml
└── schema.sql

src/test/java/com/example/identitymanager/
├── controller/
├── service/
├── repository/
├── validation/
├── model/
├── dto/
└── exception/
```

## Kluczowe implementacje

### Custom JPQL Query z JOIN
```java
@Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
List<User> findUsersByRoleName(@Param("roleName") Role.RoleName roleName);

@Query("SELECT t FROM SupportTicket t JOIN FETCH t.user WHERE t.status = :status")
List<SupportTicket> findByStatusWithUser(@Param("status") TicketStatus status);
```

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
        // ... pozostale parametry
        return ps;
    }, keyHolder);
    
    return rowsAffected;
}
```

### Custom Validators
```java
@Documented
@Constraint(validatedBy = PhoneValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhone {
    String message() default "Invalid phone number format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

public class PhoneValidator implements ConstraintValidator<ValidPhone, String> {
    private static final String PHONE_PATTERN = "^[+]?[0-9\\s\\-()]+$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return value.matches(PHONE_PATTERN);
    }
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

@ValidPhone
private String phone;

@ValidName
private String firstName;
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

## Przyklady uzycia API

### Logowanie
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "password123"
  }'
```

### Pobranie listy uzytkownikow
```bash
curl -u john@example.com:password123 \
  http://localhost:8080/api/users
```

### Utworzenie zgloszenia wsparcia (jako USER - automatycznie na siebie)
```bash
curl -u john@example.com:password123 \
  -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "subject": "Problem z aplikacja",
    "description": "Szczegolowy opis problemu"
  }'
```

### Utworzenie zgloszenia dla innego uzytkownika (tylko ADMIN)
```bash
curl -u admin@example.com:password123 \
  -X POST http://localhost:8080/api/tickets/admin \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 2,
    "subject": "Problem zgloszony przez admina",
    "description": "Szczegolowy opis problemu"
  }'
```

### Pobranie swoich zgloszen
```bash
curl -u john@example.com:password123 \
  http://localhost:8080/api/tickets/my
```

### Pobranie wszystkich zgloszen (tylko ADMIN)
```bash
curl -u admin@example.com:password123 \
  http://localhost:8080/api/tickets
```

### Zmiana statusu zgloszenia (tylko ADMIN)
```bash
curl -u admin@example.com:password123 \
  -X PATCH http://localhost:8080/api/tickets/1/status \
  -H "Content-Type: application/json" \
  -d '{
    "status": "RESOLVED"
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

### Zmiana ustawien prywatnosci
```bash
curl -u john@example.com:password123 \
  -X PATCH http://localhost:8080/api/auth/me/privacy \
  -H "Content-Type: application/json" \
  -d '{
    "isPrivacyEnabled": true
  }'
```

## Import/Export CSV

### Export uzytkownikow

Przejdz do panelu administracyjnego i kliknij "Export to CSV" lub wykonaj request:
```
GET http://localhost:8080/admin/users/export
```

Zostanie pobrany plik `users_export.csv` z danymi wszystkich uzytkownikow.

### Import uzytkownikow

1. Przygotuj plik CSV w formacie:
```
   email,firstName,lastName,phone,isPrivacyEnabled
   user@example.com,Jan,Kowalski,+48123456789,false
```

2. W panelu admina przejdz do `/admin/users/import`

3. Wybierz plik i kliknij "Import"

Uzytkownicy zostana dodani z domyslnym haslem `password123`.

## Dokumentacja API

Pelna interaktywna dokumentacja API dostepna w Swagger UI:
```
http://localhost:8080/swagger-ui.html
```

Dokumentacja zawiera opisy wszystkich endpointow, parametrow, schematow DTO oraz mozliwosc testowania API bezposrednio z przegladarki.

Swagger UI obsluguje autentykacje HTTP Basic - kliknij przycisk "Authorize" i wprowadz dane logowania.

## Wymagania spelnione

- Model danych z relacjami (User, Role, SupportTicket)
- Repository JPA z custom queries (JPQL z JOIN)
- JdbcTemplate z pelna implementacja CRUD
- Service layer z logika biznesowa
- DTO pattern dla separacji warstw
- REST API z walidacja (Bean Validation + custom validators)
- Global Exception Handler
- Spring Security z BCrypt
- Dual filter chain (API stateless, MVC stateful)
- Panel administracyjny (Thymeleaf)
- Dashboard uzytkownika z obsluga ticketow
- Upload awatarow (FileStorageService)
- Import/Export CSV (OpenCSV)
- Paginacja (JPA Pageable)
- Kontrola dostepu do ticketow (USER vs ADMIN)
- Testy jednostkowe i integracyjne (~89% coverage)
- Dokumentacja API (Swagger z autentykacja)`