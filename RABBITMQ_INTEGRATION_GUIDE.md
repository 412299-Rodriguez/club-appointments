# RabbitMQ Integration Guide

## Architecture Overview

The Training Session Management System uses RabbitMQ as a message broker to handle asynchronous notifications. This ensures reliable, scalable, and decoupled communication between the backend and notification services.

### Message Flow

```
Backend Service → MessageProducerService → RabbitMQ Queue → MessageConsumerService → n8n Webhook → Email/SMS
```

## Components

### 1. RabbitMQ Configuration ([RabbitMQConfig.java](Backend/src/main/java/com/clublosamigos/turnero/config/RabbitMQConfig.java))

Defines the messaging infrastructure:

- **Queues**:
  - `turnero.notifications` - Receives all notification events
  - `turnero.bulk-generation` - Receives bulk training session generation tasks
  - `turnero.notifications.dlq` - Dead Letter Queue for failed notifications

- **Exchange**:
  - `turnero.exchange` - Topic exchange for routing messages

- **Routing Keys**:
  - `notification.booking.confirmed` - Booking confirmation events
  - `notification.booking.cancelled` - Booking cancellation events
  - `notification.session.cancelled` - Session cancellation events
  - `notification.session.modified` - Session modification events
  - `notification.reminder.24h` - 24-hour reminder events
  - `task.bulk.generation` - Bulk generation tasks

### 2. Message Producer ([MessageProducerService.java](Backend/src/main/java/com/clublosamigos/turnero/service/MessageProducerService.java))

Publishes messages to RabbitMQ:

```java
@Service
public class MessageProducerService {

    public void publishNotification(NotificationMessage message) {
        String routingKey = getRoutingKeyForEvent(message.getEventType());
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.TURNERO_EXCHANGE,
            routingKey,
            message
        );
    }
}
```

### 3. Message Consumer ([MessageConsumerService.java](Backend/src/main/java/com/clublosamigos/turnero/service/MessageConsumerService.java))

Consumes messages from RabbitMQ and forwards to n8n:

```java
@Service
public class MessageConsumerService {

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATIONS_QUEUE)
    public void consumeNotification(NotificationMessage message) {
        // Send to n8n webhook
        // Track status in notification_logs
    }
}
```

### 4. Notification Service ([NotificationService.java](Backend/src/main/java/com/clublosamigos/turnero/service/NotificationService.java))

High-level service that triggers notifications:

```java
@Service
public class NotificationService {

    @Async
    public void sendBookingConfirmation(User user, TrainingSession session) {
        NotificationMessage message = NotificationMessage.builder()
            .eventType(NotificationEventType.BOOKING_CONFIRMED)
            .user(createUserInfo(user))
            .training(createTrainingInfo(session))
            .build();

        messageProducerService.publishNotification(message);
    }
}
```

## Message DTOs

### NotificationMessage ([NotificationMessage.java](Backend/src/main/java/com/clublosamigos/turnero/dto/message/NotificationMessage.java))

```java
@Data
@Builder
public class NotificationMessage implements Serializable {
    private NotificationEventType eventType;
    private UserInfo user;
    private TrainingInfo training;

    public static class UserInfo {
        private String email;
        private String name;
    }

    public static class TrainingInfo {
        private String name;
        private String date;
        private String time;
        private String location;
    }
}
```

## Configuration

### application.yml

```yaml
spring:
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:guest}
    password: ${RABBITMQ_PASSWORD:guest}
    listener:
      simple:
        retry:
          enabled: true
          initial-interval: 2000
          max-attempts: 3
          multiplier: 2.0

n8n:
  webhook:
    url: ${N8N_WEBHOOK_URL:http://localhost:5678/webhook/turnero-notifications}
    enabled: ${N8N_WEBHOOK_ENABLED:false}
```

### Docker Compose

```yaml
rabbitmq:
  image: rabbitmq:3.12-management-alpine
  container_name: turnero-rabbitmq
  environment:
    RABBITMQ_DEFAULT_USER: guest
    RABBITMQ_DEFAULT_PASS: guest
  ports:
    - "5672:5672"   # AMQP port
    - "15672:15672" # Management UI
  volumes:
    - rabbitmq_data:/var/lib/rabbitmq
  healthcheck:
    test: ["CMD", "rabbitmq-diagnostics", "-q", "ping"]
```

## Notification Flow Examples

### 1. Booking Confirmation

```
User creates booking
    ↓
BookingService.createBooking()
    ↓
NotificationService.sendBookingConfirmation()
    ↓
MessageProducerService.publishNotification()
    ↓
RabbitMQ Queue (turnero.notifications)
    ↓
MessageConsumerService.consumeNotification()
    ↓
RestTemplate.postForEntity(n8nWebhookUrl)
    ↓
n8n Workflow
    ↓
Email/SMS to user
```

### 2. Session Cancellation

```
Trainer cancels session
    ↓
TrainingSessionService.cancelSession()
    ↓
NotificationService.sendSessionCancellationToParticipants()
    ↓
MessageProducerService.publishNotification()
    ↓
RabbitMQ Queue
    ↓
MessageConsumerService (sends to all participants via n8n)
```

## Notification Tracking

All notifications are tracked in the `notification_logs` table:

```sql
CREATE TABLE notification_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NULL,
    event_type ENUM(...) NOT NULL,
    training_session_id BIGINT NULL,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('PENDING', 'SENT', 'FAILED') DEFAULT 'PENDING',
    error_message TEXT NULL
);
```

## Monitoring

### RabbitMQ Management UI

Access at: http://localhost:15672
- Username: `guest`
- Password: `guest`

**Available Metrics**:
- Queue depth
- Message rates (publish/deliver)
- Consumer count
- Memory usage

### Application Logs

```bash
# View consumer logs
docker logs -f turnero-backend | grep "MessageConsumerService"

# View producer logs
docker logs -f turnero-backend | grep "MessageProducerService"
```

## Error Handling

### Retry Strategy

RabbitMQ listeners are configured with automatic retry:
- Initial retry interval: 2 seconds
- Max retry attempts: 3
- Multiplier: 2.0 (exponential backoff)

### Dead Letter Queue

Failed messages after all retries are moved to `turnero.notifications.dlq`:

```java
@Bean
public Queue notificationsQueue() {
    return QueueBuilder.durable(NOTIFICATIONS_QUEUE)
        .withArgument("x-dead-letter-exchange", "")
        .withArgument("x-dead-letter-routing-key", DLQ_NOTIFICATIONS)
        .build();
}
```

### Notification Status Tracking

Each notification attempt is logged:

```java
private NotificationLog createLogEntry(NotificationMessage message) {
    return NotificationLog.builder()
        .eventType(convertEventType(message.getEventType()))
        .user(user)
        .status(NotificationStatus.PENDING)
        .build();
}

// On success
private void markLogSuccess(NotificationLog logEntry) {
    logEntry.setStatus(NotificationStatus.SENT);
    notificationLogRepository.save(logEntry);
}

// On failure
private void markLogFailure(NotificationLog logEntry, String errorMessage) {
    logEntry.setStatus(NotificationStatus.FAILED);
    logEntry.setErrorMessage(errorMessage);
    notificationLogRepository.save(logEntry);
}
```

## Testing

### Manual Testing with RabbitMQ Management UI

1. Access http://localhost:15672
2. Navigate to **Queues** tab
3. Select `turnero.notifications`
4. Use **Publish message** to send test messages

### Integration Testing

```java
@SpringBootTest
@TestPropertySource(properties = {
    "spring.rabbitmq.host=localhost",
    "spring.rabbitmq.port=5672"
})
class MessageProducerServiceTest {

    @Autowired
    private MessageProducerService messageProducerService;

    @Test
    void shouldPublishNotificationMessage() {
        NotificationMessage message = NotificationMessage.builder()
            .eventType(NotificationEventType.BOOKING_CONFIRMED)
            .user(UserInfo.builder()
                .email("test@example.com")
                .name("Test User")
                .build())
            .build();

        messageProducerService.publishNotification(message);

        // Verify message was published
    }
}
```

## Troubleshooting

### Issue: Messages not being consumed

**Check**:
1. RabbitMQ service is running: `docker ps | grep rabbitmq`
2. Queue has consumers: Check Management UI → Queues → turnero.notifications
3. Backend application logs for connection errors

**Solution**:
```bash
# Restart RabbitMQ
docker restart turnero-rabbitmq

# Restart backend
docker restart turnero-backend
```

### Issue: Messages in Dead Letter Queue

**Check**:
1. View DLQ messages in Management UI
2. Check notification_logs table for error_message

**Solution**:
```sql
-- View failed notifications
SELECT * FROM notification_logs WHERE status = 'FAILED' ORDER BY sent_at DESC;

-- Check specific error
SELECT error_message, COUNT(*) as count
FROM notification_logs
WHERE status = 'FAILED'
GROUP BY error_message;
```

### Issue: n8n webhook not receiving messages

**Check**:
1. n8n service is running: `docker ps | grep n8n`
2. Webhook URL is correct in application.yml
3. N8N_WEBHOOK_ENABLED=true in environment

**Solution**:
```bash
# Check n8n logs
docker logs turnero-n8n

# Test webhook manually
curl -X POST http://localhost:5678/webhook/turnero-notifications \
  -H "Content-Type: application/json" \
  -d '{"eventType":"BOOKING_CONFIRMED","user":{"email":"test@example.com","name":"Test"}}'
```

## Performance Considerations

### Message Throughput

- RabbitMQ can handle 20,000+ messages/second
- Current configuration is optimized for reliability over speed
- For high-volume scenarios, consider:
  - Increasing consumer count (multiple instances)
  - Using batch processing
  - Adjusting prefetch count

### Resource Usage

- RabbitMQ memory: ~256MB base + message storage
- Backend consumer threads: 1 per @RabbitListener
- Connection pooling: Managed by Spring AMQP

## Best Practices

1. **Always use @Async for notification methods** - Don't block main request thread
2. **Use DTOs (NotificationMessage)** - Don't send entity objects through RabbitMQ
3. **Track all notifications** - Use notification_logs table for auditing
4. **Handle consumer failures gracefully** - Don't throw exceptions that break message processing
5. **Monitor queue depth** - Alert if messages are backing up
6. **Use appropriate routing keys** - Enable future filtering and routing logic

## Future Enhancements

- [ ] Add message priority (urgent vs normal)
- [ ] Implement batch notification processing
- [ ] Add webhook signature verification for security
- [ ] Create notification preferences per user
- [ ] Add retry queue with longer delays
- [ ] Implement notification templates in n8n
