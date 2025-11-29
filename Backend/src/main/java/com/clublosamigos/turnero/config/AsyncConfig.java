package com.clublosamigos.turnero.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Configuration for async task execution
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    // Async configuration for notification service
    // Uses default SimpleAsyncTaskExecutor
}
