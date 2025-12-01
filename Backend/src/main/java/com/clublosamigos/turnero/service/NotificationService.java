package com.clublosamigos.turnero.service;

import com.clublosamigos.turnero.dto.message.NotificationMessage;
import com.clublosamigos.turnero.model.TrainingSession;
import com.clublosamigos.turnero.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * Service for sending notifications to users via RabbitMQ
 * Publishes notification messages to RabbitMQ which are then consumed and sent to n8n
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final MessageProducerService messageProducerService;

    /**
     * Send booking confirmation notification
     *
     * @param user User who made the booking
     * @param session Training session that was booked
     */
    @Async
    public void sendBookingConfirmation(User user, TrainingSession session) {
        log.info("Publishing booking confirmation notification for user {} and session {}",
                user.getEmail(), session.getName());

        NotificationMessage message = NotificationMessage.builder()
                .eventType(NotificationMessage.NotificationEventType.BOOKING_CONFIRMED)
                .user(createUserInfo(user))
                .training(createTrainingInfo(session))
                .build();

        messageProducerService.publishNotification(message);
    }

    /**
     * Send booking cancellation notification
     *
     * @param user User who cancelled the booking
     * @param session Training session that was cancelled
     */
    @Async
    public void sendBookingCancellation(User user, TrainingSession session) {
        log.info("Publishing booking cancellation notification for user {} and session {}",
                user.getEmail(), session.getName());

        NotificationMessage message = NotificationMessage.builder()
                .eventType(NotificationMessage.NotificationEventType.BOOKING_CANCELLED)
                .user(createUserInfo(user))
                .training(createTrainingInfo(session))
                .build();

        messageProducerService.publishNotification(message);
    }

    /**
     * Send session reminder notification
     *
     * @param user User to remind
     * @param session Upcoming training session
     */
    @Async
    public void sendSessionReminder(User user, TrainingSession session) {
        log.info("Publishing session reminder notification for user {} and session {}",
                user.getEmail(), session.getName());

        NotificationMessage message = NotificationMessage.builder()
                .eventType(NotificationMessage.NotificationEventType.REMINDER_24H)
                .user(createUserInfo(user))
                .training(createTrainingInfo(session))
                .build();

        messageProducerService.publishNotification(message);
    }

    /**
     * Send session cancellation notification to all participants
     *
     * @param session Cancelled training session
     */
    @Async
    public void sendSessionCancellationToParticipants(TrainingSession session) {
        log.info("Publishing session cancellation notification for session {}", session.getName());

        NotificationMessage message = NotificationMessage.builder()
                .eventType(NotificationMessage.NotificationEventType.SESSION_CANCELLED)
                .training(createTrainingInfo(session))
                .build();

        messageProducerService.publishNotification(message);
    }

    /**
     * Send session modified notification
     *
     * @param session Modified training session
     */
    @Async
    public void sendSessionModified(TrainingSession session) {
        log.info("Publishing session modified notification for session {}", session.getName());

        NotificationMessage message = NotificationMessage.builder()
                .eventType(NotificationMessage.NotificationEventType.SESSION_MODIFIED)
                .training(createTrainingInfo(session))
                .build();

        messageProducerService.publishNotification(message);
    }

    /**
     * Create user info DTO from User entity
     *
     * @param user User entity
     * @return NotificationMessage.UserInfo
     */
    private NotificationMessage.UserInfo createUserInfo(User user) {
        return NotificationMessage.UserInfo.builder()
                .email(user.getEmail())
                .name(user.getFullName())
                .build();
    }

    /**
     * Create training info DTO from TrainingSession entity
     *
     * @param session Training session entity
     * @return NotificationMessage.TrainingInfo
     */
    private NotificationMessage.TrainingInfo createTrainingInfo(TrainingSession session) {
        return NotificationMessage.TrainingInfo.builder()
                .name(session.getName())
                .date(session.getDate().format(DateTimeFormatter.ISO_DATE))
                .time(session.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                .location(session.getLocation())
                .build();
    }
}
