# Deployment Guide - Club Los Amigos Training Session Management

## Prerequisites

- Docker 20.10+
- Docker Compose 2.0+
- Git
- 4GB RAM minimum
- 10GB free disk space

## Architecture

The system consists of 5 containerized services:

1. **MySQL 8.0.0** - Database (Port 3306)
2. **RabbitMQ 3.12** - Message Broker (Ports 5672, 15672)
3. **Spring Boot Backend** - API Server (Port 8080)
4. **n8n** - Workflow Automation (Port 5678)
5. **Angular Frontend + NGINX** - Web Application (Port 1999)

## Quick Start

### 1. Clone the Repository

```bash
git clone <repository-url>
cd club-appointments
```

### 2. Build and Start All Services

```bash
cd Arquitectura
docker-compose up -d
```

This will:
- Pull all required Docker images
- Build the backend Spring Boot application
- Initialize MySQL database with schema and seed data
- Start RabbitMQ message broker
- Start n8n workflow automation
- Start the Angular frontend

### 3. Verify Services are Running

```bash
docker-compose ps
```

Expected output:
```
NAME                 STATUS              PORTS
turnero-mysql        Up (healthy)        0.0.0.0:3306->3306/tcp
turnero-rabbitmq     Up (healthy)        0.0.0.0:5672->5672/tcp, 0.0.0.0:15672->15672/tcp
turnero-backend      Up (healthy)        8080/tcp
turnero-n8n          Up                  0.0.0.0:5678->5678/tcp
turnero-proxy        Up                  80/tcp
turnero-frontend     Up                  0.0.0.0:1999->80/tcp
```

### 4. Access the Application

- **Frontend Application**: http://localhost:1999
- **Backend API**: http://localhost:8080/api (via proxy)
- **RabbitMQ Management**: http://localhost:15672 (guest/guest)
- **n8n Workflow Editor**: http://localhost:5678

## Default Credentials

### Application Users

| Role | Email | Password |
|------|-------|----------|
| Super Admin | admin@clublosamigos.com | Admin123! |
| Trainer | trainer@clublosamigos.com | Trainer123! |
| User | user@clublosamigos.com | User123! |

### Infrastructure

| Service | Username | Password |
|---------|----------|----------|
| MySQL | turnero_user | turnero_pass |
| MySQL (root) | root | rootpass |
| RabbitMQ | guest | guest |

## Configuration

### Environment Variables

All services can be configured via environment variables in [docker-compose.yml](Arquitectura/docker-compose.yml):

#### Backend Configuration

```yaml
environment:
  # Database
  SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/turnero_db
  SPRING_DATASOURCE_USERNAME: turnero_user
  SPRING_DATASOURCE_PASSWORD: turnero_pass

  # RabbitMQ
  RABBITMQ_HOST: rabbitmq
  RABBITMQ_PORT: 5672
  RABBITMQ_USERNAME: guest
  RABBITMQ_PASSWORD: guest

  # Security
  JWT_SECRET: club-los-amigos-secret-key-2024-production-ready-256-bits-minimum-security
  JWT_EXPIRATION: 86400000  # 24 hours in milliseconds

  # Notifications
  N8N_WEBHOOK_URL: http://n8n:5678/webhook/turnero-notifications
  N8N_WEBHOOK_ENABLED: true
  NOTIFICATION_MAX_RETRIES: 3
```

### Production Deployment

#### Security Hardening

1. **Change all default passwords**:

```yaml
environment:
  MYSQL_PASSWORD: <STRONG_PASSWORD>
  JWT_SECRET: <RANDOM_256_BIT_KEY>
  RABBITMQ_DEFAULT_PASS: <STRONG_PASSWORD>
```

2. **Use HTTPS**:

Update NGINX configuration to use SSL certificates:

```nginx
server {
    listen 443 ssl http2;
    ssl_certificate /etc/nginx/ssl/cert.pem;
    ssl_certificate_key /etc/nginx/ssl/key.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
}
```

3. **Restrict RabbitMQ Management UI**:

```yaml
rabbitmq:
  ports:
    - "127.0.0.1:15672:15672"  # Only accessible from localhost
```

#### Resource Limits

Add resource constraints in docker-compose.yml:

```yaml
services:
  backend:
    deploy:
      resources:
        limits:
          cpus: '1.0'
          memory: 1G
        reservations:
          cpus: '0.5'
          memory: 512M

  mysql:
    deploy:
      resources:
        limits:
          cpus: '2.0'
          memory: 2G
        reservations:
          cpus: '1.0'
          memory: 1G
```

#### Persistent Storage

Volumes are already configured for data persistence:

```yaml
volumes:
  mysql_data:        # MySQL database files
  rabbitmq_data:     # RabbitMQ messages and configuration
  n8n_data:          # n8n workflows and credentials
```

## Service Management

### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend
docker-compose logs -f rabbitmq
docker-compose logs -f mysql
```

### Restart Services

```bash
# All services
docker-compose restart

# Specific service
docker-compose restart backend
```

### Stop Services

```bash
# Stop all services (preserves data)
docker-compose stop

# Stop and remove containers (preserves volumes)
docker-compose down

# Stop and remove everything including volumes (CAUTION: deletes all data)
docker-compose down -v
```

### Update Services

```bash
# Rebuild backend after code changes
docker-compose build backend
docker-compose up -d backend

# Pull latest images
docker-compose pull
docker-compose up -d
```

## Database Management

### Access MySQL

```bash
# Via Docker
docker exec -it turnero-mysql mysql -u turnero_user -pturnero_pass turnero_db

# Via local MySQL client
mysql -h localhost -P 3306 -u turnero_user -pturnero_pass turnero_db
```

### Backup Database

```bash
docker exec turnero-mysql mysqldump -u turnero_user -pturnero_pass turnero_db > backup_$(date +%Y%m%d).sql
```

### Restore Database

```bash
docker exec -i turnero-mysql mysql -u turnero_user -pturnero_pass turnero_db < backup_20240101.sql
```

### Reset Database

```bash
docker-compose down
docker volume rm arquitectura_mysql_data
docker-compose up -d
```

## RabbitMQ Management

### Access Management UI

Open http://localhost:15672 and login with `guest/guest`

### Monitor Queues

Navigate to **Queues** tab to monitor:
- `turnero.notifications` - Main notification queue
- `turnero.bulk-generation` - Bulk task queue
- `turnero.notifications.dlq` - Dead letter queue for failed messages

### Purge Queue

```bash
# Via Management UI
Queues → Select queue → Purge Messages

# Via CLI
docker exec turnero-rabbitmq rabbitmqctl purge_queue turnero.notifications
```

### Check Queue Status

```bash
docker exec turnero-rabbitmq rabbitmqctl list_queues name messages consumers
```

## n8n Configuration

### Initial Setup

1. Access http://localhost:5678
2. Create admin account
3. Create webhook workflow:
   - Add **Webhook** node
   - Set path: `/webhook/turnero-notifications`
   - Add **Email** or **SMS** nodes for notifications
   - Activate workflow

### Example Notification Workflow

```
Webhook (POST /webhook/turnero-notifications)
    ↓
Switch Node (based on eventType)
    ├─ BOOKING_CONFIRMED → Send Confirmation Email
    ├─ BOOKING_CANCELLED → Send Cancellation Email
    ├─ SESSION_CANCELLED → Send Alert to All Participants
    ├─ SESSION_MODIFIED → Send Update Email
    └─ REMINDER_24H → Send Reminder SMS
```

## Monitoring and Health Checks

### Health Endpoints

- **Backend**: http://localhost:8080/actuator/health
- **MySQL**: `docker exec turnero-mysql mysqladmin ping`
- **RabbitMQ**: http://localhost:15672/api/health/checks/alarms

### Application Metrics

```bash
# View backend metrics
curl http://localhost:8080/actuator/info

# View all actuator endpoints
curl http://localhost:8080/actuator
```

### Monitor Resource Usage

```bash
docker stats
```

## Troubleshooting

### Backend won't start

**Symptoms**: Backend container keeps restarting

**Check**:
```bash
docker-compose logs backend
```

**Common Issues**:
1. MySQL not ready → Wait for health check to pass
2. RabbitMQ connection failed → Check RabbitMQ is running
3. Port 8080 already in use → Change port in docker-compose.yml

### Frontend shows 502 Bad Gateway

**Cause**: Backend not responding

**Solution**:
```bash
# Check backend health
docker-compose logs backend

# Restart backend
docker-compose restart backend
```

### RabbitMQ messages not being processed

**Check**:
```bash
# View consumer status
docker exec turnero-rabbitmq rabbitmqctl list_consumers

# Check backend logs
docker-compose logs backend | grep RabbitListener
```

**Solution**:
```bash
# Restart backend to reconnect consumers
docker-compose restart backend
```

### n8n webhook not receiving notifications

**Check**:
1. n8n workflow is activated
2. Webhook path matches: `/webhook/turnero-notifications`
3. Backend environment: `N8N_WEBHOOK_ENABLED=true`

**Test**:
```bash
curl -X POST http://localhost:5678/webhook/turnero-notifications \
  -H "Content-Type: application/json" \
  -d '{"eventType":"BOOKING_CONFIRMED","user":{"email":"test@test.com","name":"Test"}}'
```

### Database connection issues

**Check**:
```bash
# Test MySQL connection
docker exec turnero-mysql mysql -u turnero_user -pturnero_pass -e "SELECT 1"

# View MySQL logs
docker-compose logs mysql
```

**Solution**:
```bash
# Restart MySQL
docker-compose restart mysql

# Wait for health check
docker-compose ps mysql
```

## Performance Tuning

### MySQL Optimization

Edit MySQL configuration:

```yaml
mysql:
  command:
    - --max-connections=200
    - --innodb-buffer-pool-size=1G
    - --query-cache-size=64M
```

### RabbitMQ Optimization

```yaml
rabbitmq:
  environment:
    RABBITMQ_VM_MEMORY_HIGH_WATERMARK: 1GB
    RABBITMQ_DISK_FREE_LIMIT: 2GB
```

### Backend JVM Tuning

```yaml
backend:
  environment:
    JAVA_OPTS: "-Xms512m -Xmx1024m -XX:+UseG1GC"
```

## Backup Strategy

### Automated Backup Script

```bash
#!/bin/bash
BACKUP_DIR="/backups"
DATE=$(date +%Y%m%d_%H%M%S)

# Backup MySQL
docker exec turnero-mysql mysqldump -u turnero_user -pturnero_pass turnero_db \
  > $BACKUP_DIR/mysql_$DATE.sql

# Backup n8n data
docker cp turnero-n8n:/home/node/.n8n $BACKUP_DIR/n8n_$DATE

# Compress
tar -czf $BACKUP_DIR/backup_$DATE.tar.gz $BACKUP_DIR/*_$DATE*

# Clean up
rm -rf $BACKUP_DIR/*_$DATE.sql $BACKUP_DIR/n8n_$DATE

# Keep only last 30 days
find $BACKUP_DIR -name "backup_*.tar.gz" -mtime +30 -delete
```

### Schedule with Cron

```bash
# Add to crontab
0 2 * * * /path/to/backup-script.sh
```

## Scaling

### Horizontal Scaling (Multiple Backend Instances)

```yaml
backend:
  deploy:
    replicas: 3
```

### Load Balancer Configuration

Update NGINX proxy to load balance:

```nginx
upstream backend_servers {
    server backend1:8080;
    server backend2:8080;
    server backend3:8080;
}
```

## Support

For issues and questions:
- Check logs: `docker-compose logs -f`
- Review [RABBITMQ_INTEGRATION_GUIDE.md](RABBITMQ_INTEGRATION_GUIDE.md)
- Check [README.md](README.md) for project overview
