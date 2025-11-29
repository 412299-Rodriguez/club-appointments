# System Architecture
# Club Los Amigos - Training Session Management System

## Overview

This is a modern, cloud-native web application built with a microservices-inspired architecture, containerized using Docker and orchestrated with Docker Compose.

## Architecture Diagram

```
┌─────────────┐
│   Client    │
│  (Browser)  │
└──────┬──────┘
       │ HTTP :1999
       ▼
┌─────────────────────────┐
│  NGINX Web Server       │
│  (Frontend Container)   │
│  Serves Angular SPA     │
└───────┬─────────────────┘
        │ /api requests
        ▼
┌─────────────────────────┐
│  NGINX Reverse Proxy    │
│  (Proxy Container)      │
└───────┬─────────────────┘
        │ :8080
        ▼
┌─────────────────────────┐
│  Spring Boot Backend    │
│  (Backend Container)    │
│  - REST API             │
│  - JWT Authentication   │
│  - Business Logic       │
└───────┬─────────────────┘
        │ :3306
        ▼
┌─────────────────────────┐
│  MySQL Database         │
│  (MySQL Container)      │
│  - Persistent Storage   │
└─────────────────────────┘
```

## Technology Stack

### Frontend
- **Framework**: Angular 19+
- **Language**: TypeScript 5.5+
- **Styling**: Tailwind CSS
- **State Management**: Signals (Angular 19)
- **HTTP Client**: Angular HttpClient
- **Routing**: Angular Router
- **PWA**: @angular/service-worker

### Backend
- **Framework**: Spring Boot 3.2
- **Language**: Java 17
- **Security**: Spring Security + JWT
- **ORM**: Hibernate/JPA
- **Validation**: Jakarta Validation
- **Build Tool**: Maven 3.9

### Database
- **RDBMS**: MySQL 8.0.0
- **Connection Pool**: HikariCP (default in Spring Boot)

### Infrastructure
- **Containerization**: Docker
- **Orchestration**: Docker Compose
- **Web Server**: NGINX Alpine
- **Reverse Proxy**: NGINX Alpine

## Container Architecture

### 1. MySQL Container (turnero-mysql)
- **Image**: mysql:8.0.0
- **Port**: 3306 (internal)
- **Volume**: mysql_data (persistent)
- **Health Check**: mysqladmin ping
- **Initialization**: Runs SQL scripts from init-scripts/

### 2. Backend Container (turnero-backend)
- **Base Image**: eclipse-temurin:17-jre-alpine
- **Build**: Multi-stage (Maven build → JRE runtime)
- **Port**: 8080 (internal)
- **Health Check**: /actuator/health endpoint
- **Environment**: Production profile (SPRING_PROFILES_ACTIVE=prod)

### 3. Proxy Container (turnero-proxy)
- **Image**: nginx:alpine
- **Configuration**: Custom nginx.conf
- **Purpose**: Routes /api requests to backend
- **Port**: 80 (internal)

### 4. Frontend Container (turnero-frontend)
- **Base Image**: nginx:alpine
- **Build**: Multi-stage (Node build → NGINX serve)
- **Port**: 1999 (external)
- **Purpose**: Serves Angular SPA and proxies API calls

## Network Architecture

### Docker Network
- **Name**: turnero-network
- **Driver**: bridge
- **Communication**: Internal DNS resolution between containers

### Port Mapping
- **External**: 1999 → Frontend NGINX
- **Internal**: 
  - Frontend NGINX → Proxy NGINX (HTTP)
  - Proxy NGINX → Backend:8080 (HTTP)
  - Backend → MySQL:3306 (TCP)

## Data Flow

### 1. User Login Flow
```
User → Frontend :1999
  → POST /api/auth/login
  → Proxy NGINX
  → Backend :8080/auth/login
  → MySQL (validate credentials)
  ← JWT Token
  ← User Data
```

### 2. Booking Creation Flow
```
User → Frontend (with JWT in header)
  → POST /api/bookings
  → Proxy NGINX
  → Backend :8080/bookings
  → JWT Validation (SecurityFilter)
  → BookingService
  → MySQL (check capacity)
  → MySQL (create booking)
  → NotificationService (async)
  → n8n Webhook
  ← Booking confirmation
```

## Security Architecture

### Authentication
- **Method**: JWT (JSON Web Tokens)
- **Storage**: Client-side (localStorage/sessionStorage)
- **Transmission**: Authorization: Bearer <token>
- **Expiration**: 24 hours (configurable)

### Authorization
- **Roles**: SUPER_ADMIN, ENTRENADOR, USUARIO
- **Method**: Spring Security @PreAuthorize annotations
- **Enforcement**: Method-level and URL-level

### Security Measures
1. **CORS**: Configured for localhost:1999, localhost:4200
2. **CSRF**: Disabled (stateless JWT authentication)
3. **Password Encryption**: BCrypt (strength 10)
4. **SQL Injection**: Prevented by JPA/Hibernate
5. **XSS**: Angular sanitization + Content Security Policy

## Database Schema

### Tables
1. **users**: System users with roles
2. **training_sessions**: Training sessions/turns
3. **bookings**: User reservations
4. **slot_configurations**: Recurring session templates
5. **notification_logs**: Notification tracking (future)

### Relationships
- User → TrainingSession (one-to-many, as trainer)
- User → Booking (one-to-many)
- TrainingSession → Booking (one-to-many)
- SlotConfiguration → TrainingSession (one-to-many)

### Indexes
- users.email (unique)
- training_sessions.date, trainer_id, status
- bookings.user_id, training_session_id
- Composite indexes for search performance

## Scalability Considerations

### Horizontal Scaling
- **Backend**: Can run multiple instances behind load balancer
- **Frontend**: Static files, easily cached and distributed
- **Database**: Can implement read replicas

### Performance Optimization
- **Database Connection Pool**: HikariCP with optimized settings
- **HTTP Caching**: Static assets cached for 1 year
- **API Response**: DTOs to minimize payload size
- **Lazy Loading**: Angular lazy-loaded modules

### Monitoring
- **Health Checks**: All containers have health checks
- **Actuator**: Spring Boot Actuator endpoints
- **Logging**: Structured logging with configurable levels

## Deployment Strategy

### Development
```bash
docker-compose up
```

### Production
1. Build images with production tags
2. Use external MySQL (e.g., AWS RDS)
3. Configure environment variables
4. Use HTTPS/SSL certificates
5. Implement rate limiting
6. Add monitoring (Prometheus, Grafana)

## Future Enhancements

### Technical
- [ ] Redis for caching and sessions
- [ ] Kafka for event streaming
- [ ] Elasticsearch for advanced search
- [ ] MinIO/S3 for file storage
- [ ] Kubernetes deployment

### Features
- [ ] Real-time updates (WebSockets)
- [ ] Push notifications
- [ ] Analytics dashboard
- [ ] Multi-tenancy support

## Design Patterns Used

### Backend
1. **Repository Pattern**: Data access abstraction
2. **Service Layer Pattern**: Business logic separation
3. **DTO Pattern**: Data transfer objects
4. **Builder Pattern**: Entity construction
5. **Strategy Pattern**: Notification strategies
6. **Factory Pattern**: Slot configuration generation
7. **Singleton Pattern**: Spring beans

### Frontend
1. **Singleton Pattern**: Angular services
2. **Observer Pattern**: RxJS Observables
3. **Guard Pattern**: Route guards
4. **Interceptor Pattern**: HTTP interceptors
5. **Facade Pattern**: Complex service operations

## Configuration Management

### Environment-Specific
- **Development**: application.yml
- **Production**: application-prod.yml
- **Docker**: Environment variables in docker-compose.yml

### Externalized Configuration
- Database credentials
- JWT secret key
- n8n webhook URL
- Feature flags (future)

