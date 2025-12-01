package com.clublosamigos.turnero.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ Configuration
 * Defines queues, exchanges, bindings, and message converters
 */
@Configuration
public class RabbitMQConfig {

    // Queue Names
    public static final String NOTIFICATIONS_QUEUE = "turnero.notifications";
    public static final String BULK_GENERATION_QUEUE = "turnero.bulk-generation";
    public static final String DLQ_NOTIFICATIONS = "turnero.notifications.dlq";

    // Exchange Names
    public static final String TURNERO_EXCHANGE = "turnero.exchange";

    // Routing Keys
    public static final String ROUTING_KEY_BOOKING_CONFIRMED = "notification.booking.confirmed";
    public static final String ROUTING_KEY_BOOKING_CANCELLED = "notification.booking.cancelled";
    public static final String ROUTING_KEY_SESSION_CANCELLED = "notification.session.cancelled";
    public static final String ROUTING_KEY_SESSION_MODIFIED = "notification.session.modified";
    public static final String ROUTING_KEY_REMINDER_24H = "notification.reminder.24h";
    public static final String ROUTING_KEY_BULK_GENERATION = "task.bulk.generation";

    /**
     * Notifications Queue - receives all notification events
     */
    @Bean
    public Queue notificationsQueue() {
        return QueueBuilder.durable(NOTIFICATIONS_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", DLQ_NOTIFICATIONS)
                .build();
    }

    /**
     * Bulk Generation Queue - receives bulk training session generation tasks
     */
    @Bean
    public Queue bulkGenerationQueue() {
        return QueueBuilder.durable(BULK_GENERATION_QUEUE)
                .build();
    }

    /**
     * Dead Letter Queue for failed notifications
     */
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ_NOTIFICATIONS).build();
    }

    /**
     * Topic Exchange for routing messages
     */
    @Bean
    public TopicExchange turneroExchange() {
        return new TopicExchange(TURNERO_EXCHANGE);
    }

    /**
     * Bindings for notification events
     */
    @Bean
    public Binding bindingBookingConfirmed(Queue notificationsQueue, TopicExchange turneroExchange) {
        return BindingBuilder.bind(notificationsQueue).to(turneroExchange).with(ROUTING_KEY_BOOKING_CONFIRMED);
    }

    @Bean
    public Binding bindingBookingCancelled(Queue notificationsQueue, TopicExchange turneroExchange) {
        return BindingBuilder.bind(notificationsQueue).to(turneroExchange).with(ROUTING_KEY_BOOKING_CANCELLED);
    }

    @Bean
    public Binding bindingSessionCancelled(Queue notificationsQueue, TopicExchange turneroExchange) {
        return BindingBuilder.bind(notificationsQueue).to(turneroExchange).with(ROUTING_KEY_SESSION_CANCELLED);
    }

    @Bean
    public Binding bindingSessionModified(Queue notificationsQueue, TopicExchange turneroExchange) {
        return BindingBuilder.bind(notificationsQueue).to(turneroExchange).with(ROUTING_KEY_SESSION_MODIFIED);
    }

    @Bean
    public Binding bindingReminder24h(Queue notificationsQueue, TopicExchange turneroExchange) {
        return BindingBuilder.bind(notificationsQueue).to(turneroExchange).with(ROUTING_KEY_REMINDER_24H);
    }

    /**
     * Binding for bulk generation tasks
     */
    @Bean
    public Binding bindingBulkGeneration(Queue bulkGenerationQueue, TopicExchange turneroExchange) {
        return BindingBuilder.bind(bulkGenerationQueue).to(turneroExchange).with(ROUTING_KEY_BULK_GENERATION);
    }

    /**
     * Message converter - uses Jackson to serialize/deserialize messages as JSON
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate with JSON converter
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        return rabbitTemplate;
    }

    /**
     * Listener container factory with JSON converter
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        return factory;
    }
}
