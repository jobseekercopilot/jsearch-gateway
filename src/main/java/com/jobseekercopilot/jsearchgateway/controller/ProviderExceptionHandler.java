package com.jobseekercopilot.jsearchgateway.controller;

import com.jobseekercopilot.jsearchgateway.client.JSearchApiClient;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ProviderExceptionHandler {

    @ExceptionHandler(JSearchApiClient.ProviderUnavailableException.class)
    ResponseEntity<Map<String, String>> handleProviderFailure(
            JSearchApiClient.ProviderUnavailableException exception) {
        return ResponseEntity.status(exception.getStatus()).body(Map.of(
                "code", safeCode(exception),
                "message", safeMessage(exception)));
    }

    private String safeCode(
            JSearchApiClient.ProviderUnavailableException exception) {
        return switch (exception.getStatus()) {
            case TOO_MANY_REQUESTS -> "RATE_LIMITED";
            case UNAUTHORIZED, FORBIDDEN -> "CONFIGURATION_ERROR";
            default -> "PROVIDER_UNAVAILABLE";
        };
    }

    private String safeMessage(
            JSearchApiClient.ProviderUnavailableException exception) {
        return switch (exception.getStatus()) {
            case TOO_MANY_REQUESTS -> "JSearch rate limit reached";
            case UNAUTHORIZED, FORBIDDEN -> "JSearch configuration rejected";
            default -> "JSearch is temporarily unavailable";
        };
    }
}
