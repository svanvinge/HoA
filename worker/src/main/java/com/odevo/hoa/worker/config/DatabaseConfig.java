package com.odevo.hoa.worker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * General database and other configurations for the worker.
 */
@Configuration
public class DatabaseConfig {

    /**
     * Configures a WebClient bean for making HTTP requests,
     * specifically for interacting with the Gemini API.
     */
    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }
}
