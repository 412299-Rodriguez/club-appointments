package com.clublosamigos.turnero.service;

import com.clublosamigos.turnero.config.RabbitMQConfig;
import com.clublosamigos.turnero.dto.message.BulkGenerationMessage;
import com.clublosamigos.turnero.dto.message.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * Service for publishing messages to RabbitMQ
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageProducerService {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Publish notification message to RabbitMQ
     *
     * @param message NotificationMessage to publish
     */
    public void publishNotification(NotificationMessage message) {
        try {
            String routingKey = getRoutingKeyForEvent(message.getEventType());
            log.info("Publishing notification to RabbitMQ - Event: {}, Routing Key: {}",
                    message.getEventType(), routingKey);

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.TURNERO_EXCHANGE,
                    routingKey,
                    message
            );

            log.debug("Notification published successfully: {}", message);
        } catch (Exception e) {
            log.error("Error publishing notification to RabbitMQ: {}", e.getMessage(), e);
            // Don't throw - this is async and shouldn't break the main flow
        }
    }

    /**
     * Publish bulk generation task to RabbitMQ
     *
     * @param message BulkGenerationMessage to publish
     */
    public void publishBulkGenerationTask(BulkGenerationMessage message) {
        try {
            log.info("Publishing bulk generation task to RabbitMQ - SlotConfigId: {}",
                    message.getSlotConfigurationId());

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.TURNERO_EXCHANGE,
                    RabbitMQConfig.ROUTING_KEY_BULK_GENERATION,
                    message
            );

            log.debug("Bulk generation task published successfully: {}", message);
        } catch (Exception e) {
            log.error("Error publishing bulk generation task to RabbitMQ: {}", e.getMessage(), e);
        }
    }

    /**
     * Get routing key based on notification event type
     *
     * @param eventType NotificationEventType
     * @return Routing key string
     */
    private String getRoutingKeyForEvent(NotificationMessage.NotificationEventType eventType) {
        return switch (eventType) {
            case BOOKING_CONFIRMED -> RabbitMQConfig.ROUTING_KEY_BOOKING_CONFIRMED;
            case BOOKING_CANCELLED -> RabbitMQConfig.ROUTING_KEY_BOOKING_CANCELLED;
            case SESSION_CANCELLED -> RabbitMQConfig.ROUTING_KEY_SESSION_CANCELLED;
            case SESSION_MODIFIED -> RabbitMQConfig.ROUTING_KEY_SESSION_MODIFIED;
            case REMINDER_24H -> RabbitMQConfig.ROUTING_KEY_REMINDER_24H;
        };
    }
}
