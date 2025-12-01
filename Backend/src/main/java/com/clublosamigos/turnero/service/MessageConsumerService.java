package com.clublosamigos.turnero.service;

import com.clublosamigos.turnero.config.RabbitMQConfig;
import com.clublosamigos.turnero.dto.message.BulkGenerationMessage;
import com.clublosamigos.turnero.dto.message.NotificationMessage;
import com.clublosamigos.turnero.model.NotificationLog;
import com.clublosamigos.turnero.model.User;
import com.clublosamigos.turnero.repository.NotificationLogRepository;
import com.clublosamigos.turnero.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for consuming messages from RabbitMQ and processing them
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageConsumerService {

    @Value("${n8n.webhook.url}")
    private String webhookUrl;

    @Value("${n8n.webhook.enabled:false}")
    private boolean webhookEnabled;

    private final RestTemplate restTemplate;
    private final NotificationLogRepository notificationLogRepository;
    private final UserRepository userRepository;

    /**
     * Consume notification messages from RabbitMQ and send to n8n
     *
     * @param message NotificationMessage from queue
     */
    @RabbitListener(queues = RabbitMQConfig.NOTIFICATIONS_QUEUE)
    public void consumeNotification(NotificationMessage message) {
        log.info("Received notification from RabbitMQ - Event: {}", message.getEventType());

        if (!webhookEnabled) {
            log.debug("n8n webhook disabled, skipping notification");
            return;
        }

        // Create notification log entry
        NotificationLog logEntry = createLogEntry(message);

        try {
            // Build payload for n8n
            Map<String, Object> payload = new HashMap<>();
            payload.put("eventType", message.getEventType().name());

            if (message.getUser() != null) {
                payload.put("user", convertUserInfo(message.getUser()));
            }

            if (message.getTraining() != null) {
                payload.put("training", convertTrainingInfo(message.getTraining()));
            }

            // Send to n8n webhook
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            log.debug("Sending notification to n8n webhook: {}", webhookUrl);
            ResponseEntity<String> response = restTemplate.postForEntity(webhookUrl, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Notification sent successfully to n8n - Event: {}", message.getEventType());
                markLogSuccess(logEntry);
            } else {
                log.error("n8n webhook returned non-success status: {}", response.getStatusCode());
                markLogFailure(logEntry, "n8n returned status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error sending notification to n8n: {}", e.getMessage(), e);
            markLogFailure(logEntry, e.getMessage());
        }
    }

    /**
     * Consume bulk generation tasks from RabbitMQ
     *
     * @param message BulkGenerationMessage from queue
     */
    @RabbitListener(queues = RabbitMQConfig.BULK_GENERATION_QUEUE)
    public void consumeBulkGenerationTask(BulkGenerationMessage message) {
        log.info("Received bulk generation task from RabbitMQ - SlotConfigId: {}",
                message.getSlotConfigurationId());

        try {
            // TODO: Implement bulk training session generation logic
            // This would typically involve:
            // 1. Load SlotConfiguration by ID
            // 2. Generate training sessions based on configuration
            // 3. Save generated sessions to database
            // 4. Send confirmation notification

            log.info("Bulk generation task processed successfully for SlotConfigId: {}",
                    message.getSlotConfigurationId());
        } catch (Exception e) {
            log.error("Error processing bulk generation task: {}", e.getMessage(), e);
            throw e; // Re-throw to trigger retry
        }
    }

    /**
     * Create notification log entry from message
     */
    private NotificationLog createLogEntry(NotificationMessage message) {
        NotificationLog.NotificationEventType eventType = convertEventType(message.getEventType());

        User user = null;
        if (message.getUser() != null && message.getUser().getEmail() != null) {
            user = userRepository.findByEmailAndIsDeletedFalse(message.getUser().getEmail()).orElse(null);
        }

        NotificationLog logEntry = NotificationLog.builder()
                .eventType(eventType)
                .user(user)
                .status(NotificationLog.NotificationStatus.PENDING)
                .build();

        return notificationLogRepository.save(logEntry);
    }

    /**
     * Mark notification log as successful
     */
    private void markLogSuccess(NotificationLog logEntry) {
        logEntry.setStatus(NotificationLog.NotificationStatus.SENT);
        logEntry.setErrorMessage(null);
        notificationLogRepository.save(logEntry);
    }

    /**
     * Mark notification log as failed
     */
    private void markLogFailure(NotificationLog logEntry, String errorMessage) {
        logEntry.setStatus(NotificationLog.NotificationStatus.FAILED);
        logEntry.setErrorMessage(errorMessage);
        notificationLogRepository.save(logEntry);
    }

    /**
     * Convert NotificationMessage.UserInfo to Map
     */
    private Map<String, String> convertUserInfo(NotificationMessage.UserInfo userInfo) {
        Map<String, String> user = new HashMap<>();
        user.put("email", userInfo.getEmail());
        user.put("name", userInfo.getName());
        return user;
    }

    /**
     * Convert NotificationMessage.TrainingInfo to Map
     */
    private Map<String, String> convertTrainingInfo(NotificationMessage.TrainingInfo trainingInfo) {
        Map<String, String> training = new HashMap<>();
        training.put("name", trainingInfo.getName());
        training.put("date", trainingInfo.getDate());
        training.put("time", trainingInfo.getTime());
        training.put("location", trainingInfo.getLocation());
        return training;
    }

    /**
     * Convert message event type to entity event type
     */
    private NotificationLog.NotificationEventType convertEventType(
            NotificationMessage.NotificationEventType messageEventType) {
        return switch (messageEventType) {
            case BOOKING_CONFIRMED -> NotificationLog.NotificationEventType.BOOKING_CONFIRMED;
            case BOOKING_CANCELLED -> NotificationLog.NotificationEventType.BOOKING_CANCELLED;
            case SESSION_CANCELLED -> NotificationLog.NotificationEventType.SESSION_CANCELLED;
            case SESSION_MODIFIED -> NotificationLog.NotificationEventType.SESSION_MODIFIED;
            case REMINDER_24H -> NotificationLog.NotificationEventType.REMINDER_24H;
        };
    }
}
