package com.serviconli.task.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${PATIENT_SERVICE_URL:http://patient-service:8083/api/v1}")
    private String patientServiceUrl;

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder().baseUrl(patientServiceUrl);
    }
}