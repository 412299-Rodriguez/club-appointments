# Deployment Guide
# Club Los Amigos - Training Session Management System

## Quick Start (Local Development)

### Prerequisites
- Docker 20.10+
- Docker Compose 2.0+

### Steps
1. Clone repository
2. Navigate to Arquitectura folder
3. Run: docker-compose up -d
4. Access: http://localhost:1999

## Production Deployment

### 1. Update Environment Variables
Edit docker-compose.yml with secure values:
- JWT_SECRET (256+ bits)
- MYSQL_ROOT_PASSWORD
- MYSQL_PASSWORD

### 2. Enable HTTPS
Configure reverse proxy with SSL certificates

### 3. Deploy
docker-compose up -d

## Database Backup
docker exec turnero-mysql mysqldump -u turnero_user -pturnero_pass turnero_db > backup.sql

## Monitoring
docker-compose logs -f

## Health Check
curl http://localhost:1999/api/actuator/health

