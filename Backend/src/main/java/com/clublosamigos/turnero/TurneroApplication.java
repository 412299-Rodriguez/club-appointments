package com.clublosamigos.turnero;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for Club Los Amigos Training Session Management System
 *
 * @author Club Los Amigos Development Team
 * @since 1.0.0
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class TurneroApplication {

    public static void main(String[] args) {
        SpringApplication.run(TurneroApplication.class, args);
    }
}
