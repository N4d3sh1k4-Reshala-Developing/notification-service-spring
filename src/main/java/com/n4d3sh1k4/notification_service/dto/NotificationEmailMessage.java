package com.n4d3sh1k4.notification_service.dto;

public record NotificationEmailMessage(
        String email,
        String username,
        String token,
        String accountActivationTokenTtl) {
}