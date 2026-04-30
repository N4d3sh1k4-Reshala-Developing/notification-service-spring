package com.n4d3sh1k4.notification_service.dto;

public record UserCreatedMessage(
        String email,
        String username,
        String token) {
}
