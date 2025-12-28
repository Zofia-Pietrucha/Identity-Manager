# Bean Validation Summary

## DTOs with Validation Annotations

### 1. UserRegistrationDTO
**Used in:** POST /api/users (registration)

| Field | Annotations | Validation Rules |
|-------|-------------|------------------|
| email | `@NotBlank` `@Email` | Required, must be valid email format |
| password | `@NotBlank` `@Size(min=8)` | Required, minimum 8 characters |
| firstName | `@NotBlank` | Required, cannot be empty |
| lastName | `@NotBlank` | Required, cannot be empty |
| phone | - | Optional |
| isPrivacyEnabled | - | Optional, defaults to false |

**Example Invalid Request:**
```json
{
  "email": "invalid-email",
  "password": "short",
  "firstName": "",
  "lastName": "Doe"
}
```

**Expected Response (400 Bad Request):**
```json
{
  "timestamp": "2025-12-28T14:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": {
    "email": "Email must be valid",
    "password": "Password must be at least 8 characters",
    "firstName": "First name is required"
  }
}
```

---

### 2. UserUpdateDTO
**Used in:** PUT /api/me, PUT /api/users/{id}

| Field | Annotations | Validation Rules |
|-------|-------------|------------------|
| firstName | `@NotBlank` `@Size(min=2, max=50)` | Required, 2-50 characters |
| lastName | `@NotBlank` `@Size(min=2, max=50)` | Required, 2-50 characters |
| phone | `@Size(max=20)` | Optional, max 20 characters |

---

### 3. LoginRequest
**Used in:** POST /api/auth/login

| Field | Annotations | Validation Rules |
|-------|-------------|------------------|
| email | `@NotBlank` `@Email` | Required, valid email format |
| password | `@NotBlank` | Required |

---

### 4. CreateTicketRequest
**Used in:** POST /api/tickets

| Field | Annotations | Validation Rules |
|-------|-------------|------------------|
| userId | `@NotNull` | Required |
| subject | `@NotBlank` `@Size(max=200)` | Required, max 200 chars |
| description | `@NotBlank` `@Size(max=1000)` | Required, max 1000 chars |

---

## How Validation Works

1. **@Valid annotation** in controller triggers validation
2. **Spring automatically validates** the DTO before method execution
3. **If validation fails**, throws `MethodArgumentNotValidException`
4. **GlobalExceptionHandler** catches it and returns 400 with error details

## Testing Validation

Use the test endpoint: **POST /api/validation/test**

### Valid Request (200 OK):
```json
{
  "email": "valid@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe"
}
```

### Invalid Request (400 Bad Request):
```json
{
  "email": "not-an-email",
  "password": "123",
  "firstName": "",
  "lastName": "D"
}
```