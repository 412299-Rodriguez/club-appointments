package com.clublosamigos.turnero.service;

import com.clublosamigos.turnero.model.NotificationLog;
import com.clublosamigos.turnero.model.NotificationLog.NotificationEventType;
import com.clublosamigos.turnero.model.NotificationLog.NotificationStatus;
import com.clublosamigos.turnero.model.TrainingSession;
import com.clublosamigos.turnero.model.User;
import com.clublosamigos.turnero.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for sending notifications to users via n8n webhook
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    @Value("${n8n.webhook.url}")
    private String webhookUrl;

    @Value("${n8n.webhook.enabled:false}")
    private boolean webhookEnabled;

    @Value("${notifications.retry.max-attempts:3}")
    private int maxRetryAttempts;

    private final WebClient webClient = WebClient.builder().build();
    private final NotificationLogRepository notificationLogRepository;

    /**
     * Send booking confirmation notification
     *
     * @param user User who made the booking
     * @param session Training session that was booked
     */
    @Async
    public void sendBookingConfirmation(User user, TrainingSession session) {
        log.info("Sending booking confirmation to user {} for session {}", user.getEmail(), session.getName());
        
        if (!webhookEnabled) {
            log.debug("n8n webhook disabled, skipping notification");
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("eventType", "BOOKING_CONFIRMED");
        payload.put("user", createUserPayload(user));
        payload.put("training", createTrainingPayload(session));

        NotificationLog logEntry = createLog(NotificationEventType.BOOKING_CONFIRMED, user, session);
        sendWebhook(payload, logEntry);
    }

    /**
     * Send booking cancellation notification
     *
     * @param user User who cancelled the booking
     * @param session Training session that was cancelled
     */
    @Async
    public void sendBookingCancellation(User user, TrainingSession session) {
        log.info("Sending booking cancellation to user {} for session {}", user.getEmail(), session.getName());
        
        if (!webhookEnabled) {
            log.debug("n8n webhook disabled, skipping notification");
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("eventType", "BOOKING_CANCELLED");
        payload.put("user", createUserPayload(user));
        payload.put("training", createTrainingPayload(session));

        NotificationLog logEntry = createLog(NotificationEventType.BOOKING_CANCELLED, user, session);
        sendWebhook(payload, logEntry);
    }

    /**
     * Send session reminder notification
     *
     * @param user User to remind
     * @param session Upcoming training session
     */
    @Async
    public void sendSessionReminder(User user, TrainingSession session) {
        log.info("Sending session reminder to user {} for session {}", user.getEmail(), session.getName());
        
        if (!webhookEnabled) {
            log.debug("n8n webhook disabled, skipping notification");
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("eventType", "REMINDER_24H");
        payload.put("user", createUserPayload(user));
        payload.put("training", createTrainingPayload(session));

        NotificationLog logEntry = createLog(NotificationEventType.REMINDER_24H, user, session);
        sendWebhook(payload, logEntry);
    }

    /**
     * Send session cancellation notification to all participants
     *
     * @param session Cancelled training session
     */
    @Async
    public void sendSessionCancellationToParticipants(TrainingSession session) {
        log.info("Sending session cancellation notification for session {}", session.getName());
        
        if (!webhookEnabled) {
            log.debug("n8n webhook disabled, skipping notification");
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("eventType", "SESSION_CANCELLED");
        payload.put("training", createTrainingPayload(session));

        NotificationLog logEntry = createLog(NotificationEventType.SESSION_CANCELLED, null, session);
        sendWebhook(payload, logEntry);
    }

    /**
     * Send session modified notification
     *
     * @param session Modified training session
     */
    @Async
    public void sendSessionModified(TrainingSession session) {
        log.info("Sending session modified notification for session {}", session.getName());
        
        if (!webhookEnabled) {
            log.debug("n8n webhook disabled, skipping notification");
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("eventType", "SESSION_MODIFIED");
        payload.put("training", createTrainingPayload(session));

        NotificationLog logEntry = createLog(NotificationEventType.SESSION_MODIFIED, null, session);
        sendWebhook(payload, logEntry);
    }

    /**
     * Send webhook to n8n
     *
     * @param payload Notification payload
     */
    private void sendWebhook(Map<String, Object> payload, NotificationLog logEntry) {
        if (!webhookEnabled) {
            markLogFailure(logEntry, "Webhook disabled");
            return;
        }

        try {
            webClient.post()
                    .uri(webhookUrl)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .retryWhen(Retry.backoff(maxRetryAttempts, Duration.ofSeconds(2)))
                    .doOnSuccess(response -> markLogSuccess(logEntry))
                    .doOnError(error -> markLogFailure(logEntry, error.getMessage()))
                    .onErrorResume(error -> Mono.empty())
                    .subscribe();
        } catch (Exception e) {
            markLogFailure(logEntry, e.getMessage());
            log.error("Exception while sending webhook: {}", e.getMessage(), e);
        }
    }

    /**
     * Create user payload for webhook
     *
     * @param user User entity
     * @return User payload map
     */
    private Map<String, String> createUserPayload(User user) {
        Map<String, String> userPayload = new HashMap<>();
        userPayload.put("email", user.getEmail());
        userPayload.put("name", user.getFullName());
        return userPayload;
    }

    /**
     * Create training session payload for webhook
     *
     * @param session Training session entity
     * @return Training payload map
     */
    private Map<String, String> createTrainingPayload(TrainingSession session) {
        Map<String, String> trainingPayload = new HashMap<>();
        trainingPayload.put("name", session.getName());
        trainingPayload.put("date", session.getDate().format(DateTimeFormatter.ISO_DATE));
        trainingPayload.put("time", session.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        trainingPayload.put("location", session.getLocation());
        return trainingPayload;
    }

    private NotificationLog createLog(NotificationEventType eventType, User user, TrainingSession session) {
        NotificationLog logEntry = NotificationLog.builder()
                .eventType(eventType)
                .user(user)
                .trainingSession(session)
                .status(NotificationStatus.PENDING)
                .build();
        return notificationLogRepository.save(logEntry);
    }

    private void markLogSuccess(NotificationLog logEntry) {
        logEntry.setStatus(NotificationStatus.SENT);
        logEntry.setErrorMessage(null);
        notificationLogRepository.save(logEntry);
    }

    private void markLogFailure(NotificationLog logEntry, String errorMessage) {
        logEntry.setStatus(NotificationStatus.FAILED);
        logEntry.setErrorMessage(errorMessage);
        notificationLogRepository.save(logEntry);
    }
}
