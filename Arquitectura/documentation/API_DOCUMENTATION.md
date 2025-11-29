# API Documentation
# Club Los Amigos - Training Session Management System

## Base URL
- **Development**: `http://localhost:1999/api`
- **Production**: `https://your-domain.com/api`

## Authentication
All protected endpoints require a JWT token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

---

## Authentication Endpoints

### POST /auth/register
Register a new user.

**Request Body:**
```json
{
  "fullName": "Juan Pérez",
  "email": "juan.perez@example.com",
  "password": "Password123!",
  "confirmPassword": "Password123!"
}
```

**Response:** `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "id": 1,
  "email": "juan.perez@example.com",
  "fullName": "Juan Pérez",
  "role": "USUARIO"
}
```

**Errors:**
- `400 Bad Request`: Validation errors, passwords don't match
- `409 Conflict`: Email already exists

---

### POST /auth/login
Login with existing credentials.

**Request Body:**
```json
{
  "email": "juan.perez@example.com",
  "password": "Password123!"
}
```

**Response:** `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "id": 1,
  "email": "juan.perez@example.com",
  "fullName": "Juan Pérez",
  "role": "USUARIO"
}
```

**Errors:**
- `401 Unauthorized`: Invalid credentials
- `404 Not Found`: User not found

---

## Training Session Endpoints

### GET /training-sessions
Get all active training sessions.

**Auth Required:** No (public)

**Query Parameters:**
- `search` (optional): Search by name, location, or trainer
- `date` (optional): Filter by specific date (YYYY-MM-DD)

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "name": "Entrenamiento técnico - Fundamentos",
    "description": "Sesión enfocada en mejorar la técnica individual",
    "trainer": {
      "id": 2,
      "fullName": "Diego Martínez",
      "email": "diego.martinez@clublosamigos.com",
      "role": "ENTRENADOR"
    },
    "date": "2024-11-28",
    "startTime": "09:00:00",
    "endTime": "11:00:00",
    "location": "Cancha Principal",
    "maxParticipants": 20,
    "currentParticipants": 3,
    "status": "ACTIVE",
    "bookings": [
      {
        "id": 1,
        "user": {
          "id": 5,
          "fullName": "Juan Pérez",
          "email": "juan.perez@example.com",
          "role": "USUARIO"
        },
        "status": "CONFIRMED",
        "createdAt": "2024-11-25T10:30:00"
      }
    ]
  }
]
```

---

### GET /training-sessions/{id}
Get a specific training session by ID.

**Auth Required:** No (public)

**Response:** `200 OK`
```json
{
  "id": 1,
  "name": "Entrenamiento técnico - Fundamentos",
  "description": "Sesión enfocada en mejorar la técnica individual",
  "trainer": { ... },
  "date": "2024-11-28",
  "startTime": "09:00:00",
  "endTime": "11:00:00",
  "location": "Cancha Principal",
  "maxParticipants": 20,
  "currentParticipants": 3,
  "status": "ACTIVE",
  "bookings": [ ... ]
}
```

**Errors:**
- `404 Not Found`: Training session not found

---

### POST /training-sessions
Create a new training session.

**Auth Required:** Yes (ENTRENADOR or SUPER_ADMIN)

**Request Body:**
```json
{
  "name": "Táctica y juego posicional",
  "description": "Desarrollo de conceptos tácticos",
  "trainerId": 2,
  "date": "2024-12-01",
  "startTime": "19:00:00",
  "endTime": "21:00:00",
  "location": "Gimnasio Techado",
  "maxParticipants": 12
}
```

**Response:** `201 Created`
```json
{
  "id": 7,
  "name": "Táctica y juego posicional",
  ...
}
```

**Errors:**
- `400 Bad Request`: Validation errors
- `401 Unauthorized`: Not authenticated
- `403 Forbidden`: Insufficient permissions

---

### PUT /training-sessions/{id}
Update an existing training session.

**Auth Required:** Yes (ENTRENADOR owns session or SUPER_ADMIN)

**Request Body:** Same as POST

**Response:** `200 OK`

**Errors:**
- `400 Bad Request`: Validation errors
- `401 Unauthorized`: Not authenticated
- `403 Forbidden`: Not the owner or not admin
- `404 Not Found`: Training session not found

---

### DELETE /training-sessions/{id}
Soft delete a training session.

**Auth Required:** Yes (SUPER_ADMIN only)

**Response:** `204 No Content`

**Errors:**
- `401 Unauthorized`: Not authenticated
- `403 Forbidden`: Not a super admin
- `404 Not Found`: Training session not found

---

## Booking Endpoints

### GET /bookings/my-bookings
Get current user's bookings.

**Auth Required:** Yes

**Query Parameters:**
- `status` (optional): Filter by status (UPCOMING, PAST)

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "trainingSession": {
      "id": 1,
      "name": "Entrenamiento técnico - Fundamentos",
      "date": "2024-11-28",
      "startTime": "09:00:00",
      "endTime": "11:00:00",
      "location": "Cancha Principal",
      "trainer": { ... }
    },
    "status": "CONFIRMED",
    "createdAt": "2024-11-25T10:30:00"
  }
]
```

---

### POST /bookings
Create a new booking.

**Auth Required:** Yes

**Request Body:**
```json
{
  "trainingSessionId": 1
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "trainingSession": { ... },
  "status": "CONFIRMED",
  "createdAt": "2024-11-25T10:30:00"
}
```

**Errors:**
- `400 Bad Request`: Session full, already booked, session in past
- `401 Unauthorized`: Not authenticated
- `404 Not Found`: Training session not found

---

### DELETE /bookings/{id}
Cancel a booking (soft delete).

**Auth Required:** Yes (owner of booking)

**Response:** `204 No Content`

**Errors:**
- `401 Unauthorized`: Not authenticated
- `403 Forbidden`: Not the owner of the booking
- `404 Not Found`: Booking not found
- `400 Bad Request`: Cannot cancel within 2 hours of start time

---

## User Endpoints

### GET /users/profile
Get current user's profile.

**Auth Required:** Yes

**Response:** `200 OK`
```json
{
  "id": 1,
  "fullName": "Juan Pérez",
  "email": "juan.perez@example.com",
  "role": "USUARIO"
}
```

---

### PUT /users/profile
Update current user's profile.

**Auth Required:** Yes

**Request Body:**
```json
{
  "fullName": "Juan Carlos Pérez",
  "email": "juan.perez@example.com"
}
```

**Response:** `200 OK`

**Errors:**
- `400 Bad Request`: Validation errors
- `409 Conflict`: Email already in use

---

## Slot Configuration Endpoints

### POST /slot-configs
Create a slot configuration for recurring sessions.

**Auth Required:** Yes (SUPER_ADMIN only)

**Request Body:**
```json
{
  "name": "Tandas Semanales Fundamentos",
  "recurrenceType": "WEEKLY",
  "daysOfWeek": "1,3,5",
  "startDate": "2024-12-01",
  "endDate": "2025-03-01"
}
```

**Response:** `201 Created`

---

### POST /slot-configs/{id}/generate
Generate training sessions from a slot configuration.

**Auth Required:** Yes (SUPER_ADMIN only)

**Request Body:**
```json
{
  "templateSession": {
    "name": "Entrenamiento técnico - Fundamentos",
    "description": "Sesión enfocada en mejorar la técnica",
    "trainerId": 2,
    "startTime": "09:00:00",
    "endTime": "11:00:00",
    "location": "Cancha Principal",
    "maxParticipants": 20
  }
}
```

**Response:** `200 OK`
```json
{
  "message": "12 training sessions created successfully"
}
```

---

## Error Responses

All error responses follow this format:

```json
{
  "timestamp": "2024-11-25T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for field 'email': must be a valid email",
  "path": "/api/auth/register"
}
```

### HTTP Status Codes
- `200 OK`: Success
- `201 Created`: Resource created successfully
- `204 No Content`: Success with no response body
- `400 Bad Request`: Validation error or business rule violation
- `401 Unauthorized`: Missing or invalid authentication
- `403 Forbidden`: Insufficient permissions
- `404 Not Found`: Resource not found
- `409 Conflict`: Resource already exists
- `500 Internal Server Error`: Server error

---

## Rate Limiting
(Future feature)
- 100 requests per minute per IP
- 1000 requests per hour per authenticated user

---

## Pagination
(Future feature)
```
GET /training-sessions?page=0&size=10&sort=date,asc
```

