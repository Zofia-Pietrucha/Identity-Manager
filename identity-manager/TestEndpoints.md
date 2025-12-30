# KOMPLETNY PRZEWODNIK TESTOWANIA W SWAGGER

## WAŻNE - PRZED TESTOWANIEM

Większość endpointów wymaga logowania!

### Krok 1: Zaloguj się przez przeglądarkę
```
http://localhost:8080/login
Email: admin@example.com
Password: password123
```

### Krok 2: Otwórz Swagger
```
http://localhost:8080/swagger-ui/index.html
```

Teraz możesz testować!

---

## AUTHENTICATION (AuthController)

### 1. POST /api/auth/login
**Opis:** Logowanie użytkownika

**Body:**
```json
{
  "email": "admin@example.com",
  "password": "password123"
}
```

**Oczekiwana odpowiedź - Status 200:**
```json
{
  "status": "success",
  "message": "Login successful",
  "accessToken": "YWRtaW5AZXhhbXBsZS5jb206MTczNTUyMzQ1Njc4OQ==",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "id": 1,
    "email": "admin@example.com",
    "firstName": "Admin",
    "lastName": "User",
    "phone": "123456789",
    "isPrivacyEnabled": false,
    "roles": ["USER", "ADMIN"]
  }
}
```

**Błędne dane - Status 401:**
```json
{
  "status": "error",
  "message": "Invalid email or password"
}
```

---

### 2. GET /api/auth/me
**Opis:** Pobierz dane zalogowanego użytkownika

**Parametry:** Brak

**Oczekiwana odpowiedź - Status 200:**
```json
{
  "id": 1,
  "email": "admin@example.com",
  "firstName": "Admin",
  "lastName": "User",
  "phone": "123456789",
  "isPrivacyEnabled": false,
  "roles": ["USER", "ADMIN"],
  "createdAt": "2025-12-30T01:00:00",
  "updatedAt": "2025-12-30T01:00:00"
}
```

**Jeśli nie zalogowany - Status 404:**
```json
{
  "timestamp": "2025-12-30T02:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "No authenticated user found"
}
```

---

### 3. PUT /api/auth/me
**Opis:** Edytuj dane zalogowanego użytkownika

**Body:**
```json
{
  "firstName": "Updated",
  "lastName": "Admin",
  "phone": "999888777"
}
```

**Oczekiwana odpowiedź - Status 200:**
```json
{
  "id": 1,
  "email": "admin@example.com",
  "firstName": "Updated",
  "lastName": "Admin",
  "phone": "999888777",
  "isPrivacyEnabled": false,
  "roles": ["USER", "ADMIN"]
}
```

---

### 4. PATCH /api/auth/me/privacy
**Opis:** Zmień ustawienia prywatności

**Body:**
```json
{
  "isPrivacyEnabled": true
}
```

**Oczekiwana odpowiedź - Status 200:**
```json
{
  "id": 1,
  "email": "admin@example.com",
  "firstName": "Admin",
  "lastName": "User",
  "phone": "123456789",
  "isPrivacyEnabled": true,
  "roles": ["USER", "ADMIN"]
}
```

---

## USER MANAGEMENT (UserController)

### 5. POST /api/users
**Opis:** Rejestracja nowego użytkownika

**Body:**
```json
{
  "email": "newuser@example.com",
  "password": "NewPass123!",
  "firstName": "New",
  "lastName": "User",
  "phone": "123456789",
  "isPrivacyEnabled": false
}
```

**Oczekiwana odpowiedź - Status 201:**
```json
{
  "id": 4,
  "email": "newuser@example.com",
  "firstName": "New",
  "lastName": "User",
  "phone": "123456789",
  "isPrivacyEnabled": false,
  "roles": ["USER"],
  "createdAt": "2025-12-30T02:00:00",
  "updatedAt": "2025-12-30T02:00:00"
}
```

**Email już istnieje - Status 409:**
```json
{
  "timestamp": "2025-12-30T02:00:00",
  "status": 409,
  "error": "Conflict",
  "message": "User with email newuser@example.com already exists"
}
```

**Błąd walidacji - Status 400:**
```json
{
  "timestamp": "2025-12-30T02:00:00",
  "status": 400,
  "error": "Validation Failed",
  "errors": {
    "email": "must be a well-formed email address",
    "password": "must not be blank"
  }
}
```

---

### 6. GET /api/users
**Opis:** Pobierz wszystkich użytkowników (tylko ADMIN)

**Parametry:** Brak

**Oczekiwana odpowiedź - Status 200:**
```json
[
  {
    "id": 1,
    "email": "admin@example.com",
    "firstName": "Admin",
    "lastName": "User",
    "phone": "123456789",
    "isPrivacyEnabled": false,
    "roles": ["USER", "ADMIN"]
  },
  {
    "id": 2,
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "987654321",
    "isPrivacyEnabled": true,
    "roles": ["USER"]
  }
]
```

---

### 7. GET /api/users/paginated
**Opis:** Pobierz użytkowników z paginacją

**Parametry:**
- page: 0 (domyślnie)
- size: 10 (domyślnie)
- sortBy: id (domyślnie)
- direction: asc (domyślnie)

**Przykład:** ?page=0&size=5&sortBy=email&direction=desc

**Oczekiwana odpowiedź - Status 200:**
```json
{
  "users": [
    {
      "id": 1,
      "email": "admin@example.com",
      "firstName": "Admin",
      "lastName": "User"
    }
  ],
  "currentPage": 0,
  "totalItems": 3,
  "totalPages": 1
}
```

---

### 8. GET /api/users/search
**Opis:** Wyszukaj użytkowników

**Parametry:**
- keyword: john (wymagane)
- page: 0 (domyślnie)
- size: 10 (domyślnie)

**Przykład:** ?keyword=john&page=0&size=10

**Oczekiwana odpowiedź - Status 200:**
```json
{
  "users": [
    {
      "id": 2,
      "email": "john@example.com",
      "firstName": "John",
      "lastName": "Doe"
    }
  ],
  "currentPage": 0,
  "totalItems": 1,
  "totalPages": 1
}
```

---

### 9. GET /api/users/by-role/{roleName}
**Opis:** Pobierz użytkowników po roli

**Parametry:**
- roleName: USER lub ADMIN

**Przykład:** /api/users/by-role/USER

**Oczekiwana odpowiedź - Status 200:**
```json
[
  {
    "id": 2,
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "roles": ["USER"]
  },
  {
    "id": 3,
    "email": "jane@example.com",
    "firstName": "Jane",
    "lastName": "Smith",
    "roles": ["USER"]
  }
]
```

**Nieprawidłowa rola - Status 400:**
```json
{
  "timestamp": "2025-12-30T02:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid role name: SUPERUSER. Valid values are: USER, ADMIN"
}
```

---

### 10. GET /api/users/stats/privacy
**Opis:** Statystyki prywatności

**Parametry:** Brak

**Oczekiwana odpowiedź - Status 200:**
```json
{
  "usersWithPrivacyEnabled": 2
}
```

---

### 11. GET /api/users/{id}
**Opis:** Pobierz użytkownika po ID

**Parametry:**
- id: 1

**Oczekiwana odpowiedź - Status 200:**
```json
{
  "id": 1,
  "email": "admin@example.com",
  "firstName": "Admin",
  "lastName": "User",
  "phone": "123456789",
  "isPrivacyEnabled": false,
  "roles": ["USER", "ADMIN"]
}
```

**Użytkownik nie istnieje - Status 404:**
```json
{
  "timestamp": "2025-12-30T02:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "User not found with id: 999"
}
```

---

### 12. GET /api/users/email/{email}
**Opis:** Pobierz użytkownika po emailu

**Parametry:**
- email: admin@example.com

**Oczekiwana odpowiedź - Status 200:**
```json
{
  "id": 1,
  "email": "admin@example.com",
  "firstName": "Admin",
  "lastName": "User"
}
```

---

### 13. PUT /api/users/{id}
**Opis:** Edytuj użytkownika (ADMIN)

**Parametry:**
- id: 2

**Body:**
```json
{
  "firstName": "Updated",
  "lastName": "John",
  "phone": "111222333"
}
```

**Oczekiwana odpowiedź - Status 200:**
```json
{
  "id": 2,
  "email": "john@example.com",
  "firstName": "Updated",
  "lastName": "John",
  "phone": "111222333"
}
```

---

### 14. DELETE /api/users/{id}
**Opis:** Usuń użytkownika (ADMIN)

**Parametry:**
- id: 3

**Oczekiwana odpowiedź - Status 204 No Content**
(Brak body w odpowiedzi - to jest OK!)

---

## AVATAR ENDPOINTS (UserController)

### 15. POST /api/users/{id}/avatar
**Opis:** Upload avatara dla użytkownika

**Parametry:**
- id: 1
- file: Wybierz plik (JPG/PNG/GIF/WEBP)

**Kroki w Swagger:**
1. Wpisz ID: 1
2. W polu file kliknij "Choose File"
3. Wybierz obrazek (max 5MB)
4. Kliknij "Execute"

**Oczekiwana odpowiedź - Status 200:**
```json
{
  "message": "Avatar uploaded successfully",
  "filename": "84f42019-237b-4397-8539-f8e5fae3860d.jpg"
}
```

**Nieprawidłowy typ pliku - Status 400:**
```json
{
  "timestamp": "2025-12-30T02:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Only image files are allowed"
}
```

**Pusty plik - Status 400:**
```json
{
  "timestamp": "2025-12-30T02:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "File is empty"
}
```

---

### 16. GET /api/users/{id}/avatar
**Opis:** Pobierz avatar użytkownika

**Parametry:**
- id: 1

**Oczekiwana odpowiedź - Status 200:**
- Content-Type: image/jpeg
- Body: Obrazek (wyświetlony w przeglądarce)

**Lub otwórz bezpośrednio:**
```
http://localhost:8080/api/users/1/avatar
```

**Brak avatara - Status 404:**
```json
{
  "timestamp": "2025-12-30T02:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Avatar not found for user with id: 1"
}
```

---

### 17. DELETE /api/users/{id}/avatar
**Opis:** Usuń avatar użytkownika

**Parametry:**
- id: 1

**Oczekiwana odpowiedź - Status 200:**
```json
{
  "message": "Avatar deleted successfully"
}
```

**Brak avatara do usunięcia - Status 404:**
```json
{
  "timestamp": "2025-12-30T02:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Avatar not found for user with id: 1"
}
```

---

## SUPPORT TICKETS (SupportTicketController)

### 18. GET /api/tickets
**Opis:** Pobierz wszystkie tickety

**Parametry:** Brak

**Oczekiwana odpowiedź - Status 200:**
```json
[
  {
    "id": 1,
    "subject": "Cannot login",
    "description": "I forgot my password and cannot login to my account",
    "status": "OPEN",
    "createdAt": "2025-12-30T01:00:00",
    "userEmail": "john@example.com"
  },
  {
    "id": 2,
    "subject": "Profile update issue",
    "description": "When I try to update my profile, I get an error",
    "status": "IN_PROGRESS",
    "createdAt": "2025-12-30T01:00:00",
    "userEmail": "john@example.com"
  }
]
```

---

### 19. GET /api/tickets/{id}
**Opis:** Pobierz ticket po ID

**Parametry:**
- id: 1

**Oczekiwana odpowiedź - Status 200:**
```json
{
  "id": 1,
  "subject": "Cannot login",
  "description": "I forgot my password and cannot login to my account",
  "status": "OPEN",
  "createdAt": "2025-12-30T01:00:00",
  "userEmail": "john@example.com"
}
```

**Ticket nie istnieje - Status 404:**
```json
{
  "timestamp": "2025-12-30T02:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Ticket not found with id: 999"
}
```

---

### 20. GET /api/tickets/user/{userId}
**Opis:** Pobierz tickety użytkownika

**Parametry:**
- userId: 2

**Oczekiwana odpowiedź - Status 200:**
```json
[
  {
    "id": 1,
    "subject": "Cannot login",
    "description": "I forgot my password",
    "status": "OPEN",
    "createdAt": "2025-12-30T01:00:00",
    "userEmail": "john@example.com"
  },
  {
    "id": 2,
    "subject": "Profile update issue",
    "description": "Error updating profile",
    "status": "IN_PROGRESS",
    "createdAt": "2025-12-30T01:00:00",
    "userEmail": "john@example.com"
  }
]
```

---

### 21. POST /api/tickets
**Opis:** Utwórz nowy ticket

**Body:**
```json
{
  "userId": 2,
  "subject": "Problem z logowaniem",
  "description": "Nie mogę się zalogować do aplikacji. Proszę o pomoc."
}
```

**Oczekiwana odpowiedź - Status 201:**
```json
{
  "id": 4,
  "subject": "Problem z logowaniem",
  "description": "Nie mogę się zalogować do aplikacji. Proszę o pomoc.",
  "status": "OPEN",
  "createdAt": "2025-12-30T02:30:00",
  "userEmail": "john@example.com"
}
```

**Błąd walidacji - Status 400:**
```json
{
  "timestamp": "2025-12-30T02:00:00",
  "status": 400,
  "error": "Validation Failed",
  "errors": {
    "subject": "must not be blank",
    "description": "must not be blank"
  }
}
```

**Użytkownik nie istnieje - Status 404:**
```json
{
  "timestamp": "2025-12-30T02:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "User not found with id: 999"
}
```

---

### 22. PATCH /api/tickets/{id}/status
**Opis:** Zmień status ticketu (ADMIN)

**Parametry:**
- id: 1

**Body:**
```json
{
  "status": "RESOLVED"
}
```

**Dostępne statusy:**
- OPEN
- IN_PROGRESS
- RESOLVED
- CLOSED

**Oczekiwana odpowiedź - Status 200:**
```json
{
  "id": 1,
  "subject": "Cannot login",
  "description": "I forgot my password",
  "status": "RESOLVED",
  "createdAt": "2025-12-30T01:00:00",
  "userEmail": "john@example.com"
}
```

**Nieprawidłowy status - Status 400:**
```json
{
  "timestamp": "2025-12-30T02:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid status. Valid values are: OPEN, IN_PROGRESS, RESOLVED, CLOSED"
}
```

---

## PODSUMOWANIE WSZYSTKICH ENDPOINTÓW

### Authentication (4 endpointy)
1. POST /api/auth/login - Logowanie
2. GET /api/auth/me - Pobierz swoje dane
3. PUT /api/auth/me - Edytuj swoje dane
4. PATCH /api/auth/me/privacy - Zmień prywatność

### User Management (10 endpointów)
5. POST /api/users - Rejestracja
6. GET /api/users - Lista wszystkich
7. GET /api/users/paginated - Lista z paginacją
8. GET /api/users/search - Wyszukiwanie
9. GET /api/users/by-role/{roleName} - Po roli
10. GET /api/users/stats/privacy - Statystyki
11. GET /api/users/{id} - Po ID
12. GET /api/users/email/{email} - Po email
13. PUT /api/users/{id} - Edycja
14. DELETE /api/users/{id} - Usuwanie

### Avatar Management (3 endpointy)
15. POST /api/users/{id}/avatar - Upload
16. GET /api/users/{id}/avatar - Pobierz
17. DELETE /api/users/{id}/avatar - Usuń

### Support Tickets (5 endpointów)
18. GET /api/tickets - Lista wszystkich
19. GET /api/tickets/{id} - Po ID
20. GET /api/tickets/user/{userId} - Po użytkowniku
21. POST /api/tickets - Utwórz nowy
22. PATCH /api/tickets/{id}/status - Zmień status

---

## SZYBKI CHECKLIST DO TESTOWANIA

### Podstawowe (zacznij od tego):
- [ ] POST /api/auth/login (zaloguj się)
- [ ] GET /api/auth/me (sprawdź czy jesteś zalogowany)
- [ ] POST /api/users (zarejestruj nowego użytkownika)
- [ ] POST /api/tickets (utwórz ticket)

### Edycja danych:
- [ ] PUT /api/auth/me (edytuj swoje dane)
- [ ] PATCH /api/auth/me/privacy (zmień prywatność)
- [ ] PUT /api/users/{id} (edytuj jako admin)

### Avatary:
- [ ] POST /api/users/1/avatar (upload)
- [ ] GET /api/users/1/avatar (pobierz)
- [ ] DELETE /api/users/1/avatar (usuń)

### Wyszukiwanie:
- [ ] GET /api/users/search?keyword=john
- [ ] GET /api/users/by-role/USER
- [ ] GET /api/users/paginated?page=0&size=5

### Tickety:
- [ ] GET /api/tickets (lista)
- [ ] GET /api/tickets/user/2 (po użytkowniku)
- [ ] PATCH /api/tickets/1/status (zmień status)

### Cleanup:
- [ ] DELETE /api/users/4 (usuń testowego użytkownika)

---

## NAJCZĘSTSZE BŁĘDY

### 401 Unauthorized
**Przyczyna:** Nie jesteś zalogowany
**Rozwiązanie:** Zaloguj się przez http://localhost:8080/login

### 403 Forbidden
**Przyczyna:** Nie masz uprawnień (endpoint tylko dla ADMIN)
**Rozwiązanie:** Zaloguj się jako admin@example.com

### 404 Not Found
**Przyczyna:** Zasób nie istnieje (zły ID)
**Rozwiązanie:** Sprawdź czy ID jest poprawne

### 400 Bad Request
**Przyczyna:** Błędne dane w body (walidacja)
**Rozwiązanie:** Sprawdź czy wszystkie wymagane pola są wypełnione

### 409 Conflict
**Przyczyna:** Użytkownik o takim emailu już istnieje
**Rozwiązanie:** Użyj innego emaila

