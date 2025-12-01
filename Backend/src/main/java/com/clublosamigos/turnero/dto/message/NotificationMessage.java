package com.clublosamigos.turnero.dto.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for notification messages sent through RabbitMQ
 * Must be Serializable to be sent through message broker
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private NotificationEventType eventType;
    private UserInfo user;
    private TrainingInfo training;

    /**
     * User information for notification
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        private String email;
        private String name;
    }

    /**
     * Training session information for notification
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrainingInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        private String name;
        private String date;
        private String time;
        private String location;
    }

    /**
     * Notification event types
     */
    public enum NotificationEventType {
        BOOKING_CONFIRMED,
        BOOKING_CANCELLED,
        SESSION_CANCELLED,
        SESSION_MODIFIED,
        REMINDER_24H
    }
}
