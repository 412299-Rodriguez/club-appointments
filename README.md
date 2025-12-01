# Club Social y Deportivo Los Amigos - Training Session Management System

## Project Description
Complete training session management system for Club Social y Deportivo Los Amigos, a sports club founded in 1997 in Córdoba. The system allows users to book training sessions, manage reservations, and administrators to manage training schedules.

## Technologies Used

### Frontend
- **Angular 19+** - Modern framework with signals, defer, standalone components
- **Tailwind CSS** - Utility-first CSS framework
- **PWA** - Progressive Web App capabilities
- **NGINX** - Web server

### Backend
- **Java 17** - Programming language
- **Spring Boot 3.2.0** - Application framework
- **Spring Security** - JWT authentication
- **MySQL 8.0.0** - Relational database
- **JPA/Hibernate** - ORM
- **RabbitMQ 3.12** - Message broker for async notifications

### Infrastructure
- **Docker** - Containerization
- **Docker Compose** - Multi-container orchestration
- **NGINX** - Reverse proxy
- **n8n** - Workflow automation for notifications

## Project Structure
```
club-appointments/
├── Arquitectura/
│   ├── docker-compose.yml
│   ├── nginx/
│   │   ├── nginx-frontend.conf
│   │   └── nginx-proxy.conf
│   └── documentation/
│       ├── ARCHITECTURE.md
│       ├── API_DOCUMENTATION.md
│       └── DEPLOYMENT.md
├── Frontend/
│   ├── Dockerfile
│   └── src/
├── Backend/
│   ├── Dockerfile
│   └── src/
└── README.md
```

## Installation and Setup

### Prerequisites
- Docker (version 20.10+)
- Docker Compose (version 2.0+)
- Git

### Quick Start
1. Clone the repository:
```bash
git clone <repository-url>
cd club-appointments
```

2. Navigate to the architecture folder:
```bash
cd Arquitectura
```

3. Build and start all services:
```bash
docker-compose build
docker-compose up -d
```

4. Wait for all services to be healthy (approximately 2-3 minutes):
```bash
docker-compose ps
```

5. Access the application:
- **Frontend**: http://localhost:1999
- **Backend API**: http://localhost:1999/api

### Stop Services
```bash
cd Arquitectura
docker-compose down
```

### Stop and Remove Volumes (Clean Start)
```bash
cd Arquitectura
docker-compose down -v
```

## Environment Variables

### Backend (.env or docker-compose.yml)
```
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/turnero_db
SPRING_DATASOURCE_USERNAME=turnero_user
SPRING_DATASOURCE_PASSWORD=turnero_pass
RABBITMQ_HOST=rabbitmq
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest
JWT_SECRET=your-secret-key-change-in-production-min-256-bits
JWT_EXPIRATION=86400000
N8N_WEBHOOK_URL=http://n8n:5678/webhook/turnero-notifications
N8N_WEBHOOK_ENABLED=true
```

### Frontend (environment.prod.ts)
```
API_URL=/api
```

## Default Credentials

### Super Admin
- **Email**: admin@clublosamigos.com
- **Password**: Admin123!

### Entrenador (Trainer)
- **Email**: diego.martinez@clublosamigos.com
- **Password**: Trainer123!

### Usuario (User)
- **Email**: juan.perez@example.com
- **Password**: User123!

## Docker Services

### Services Overview
- **mysql**: MySQL 8.0.0 database (port 3306)
- **rabbitmq**: RabbitMQ 3.12 message broker (ports 5672, 15672)
- **backend**: Spring Boot application (internal port 8080)
- **n8n**: Workflow automation (port 5678)
- **proxy**: NGINX reverse proxy
- **frontend**: Angular app served by NGINX (port 1999)

### Access Points
- **Frontend Application**: http://localhost:1999
- **RabbitMQ Management UI**: http://localhost:15672 (guest/guest)
- **n8n Workflow Editor**: http://localhost:5678

### View Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f mysql
```

### Restart a Service
```bash
docker-compose restart backend
docker-compose restart frontend
```

## Development

### Backend Development
```bash
cd Backend
mvn spring-boot:run
```

### Frontend Development
```bash
cd Frontend
npm install
npm start
```

## Testing

### Backend Tests
```bash
cd Backend
mvn test
```

### Frontend Tests
```bash
cd Frontend
npm test
```

## Documentation

- **[DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md)** - Complete deployment and operations guide
- **[RABBITMQ_INTEGRATION_GUIDE.md](RABBITMQ_INTEGRATION_GUIDE.md)** - RabbitMQ integration architecture and troubleshooting
- **[API_DOCUMENTATION.md](Arquitectura/documentation/API_DOCUMENTATION.md)** - REST API endpoints (if exists)
- **[ARCHITECTURE.md](Arquitectura/documentation/ARCHITECTURE.md)** - System architecture (if exists)

## Features

### User Features
- ✅ User registration and authentication (JWT)
- ✅ View available training sessions
- ✅ Book training sessions
- ✅ Cancel bookings
- ✅ View my bookings (upcoming and past)
- ✅ Search and filter training sessions
- ✅ User profile management

### Trainer Features
- ✅ All user features
- ✅ Create training sessions
- ✅ Edit own training sessions
- ✅ View participants list
- ✅ Cancel training sessions

### Admin Features
- ✅ All trainer features
- ✅ Manage all training sessions
- ✅ Create slot configurations (recurring sessions)
- ✅ Delete training sessions (logical delete)
- ✅ View all system data

### System Features
- ✅ Responsive design (mobile-first)
- ✅ PWA capabilities
- ✅ Real-time availability updates
- ✅ Soft delete (logical delete) for all entities
- ✅ Asynchronous notifications via RabbitMQ
- ✅ Email/SMS notifications (via n8n workflow automation)
- ✅ Notification tracking and audit logs
- ✅ Role-based access control (RBAC)
- ✅ Health checks and monitoring endpoints

## Future Features
- 🔄 Tournament management
- 🔄 Payment integration
- 🔄 Image gallery (facilities)
- 🔄 Club history timeline
- 🔄 Match scheduling
- 🔄 Player statistics
- 🔄 Push notifications

## Contributing
This is a private project for Club Social y Deportivo Los Amigos.

## License
Proprietary - All rights reserved to Club Social y Deportivo Los Amigos

## Support
For issues or questions, contact: admin@clublosamigos.com

## Acknowledgments
- Club Social y Deportivo Los Amigos - Córdoba
- Founded in 1997
