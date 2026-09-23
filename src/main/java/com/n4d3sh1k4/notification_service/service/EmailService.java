package com.n4d3sh1k4.notification_service.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    @Value("${email.support}")
    String supportEmail;

    @Value("${spring.mail.from}")
    String from;

    @Value("${app.api-url}")
    String apiUrl;

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public void sendRegistrationEmail(String to, String username, String token, String accountActivationTokenTtl) {
        Context context = new Context();
        if (token != null) {
            context.setVariable("username", username);
            context.setVariable("activationUrl", apiUrl + "/api/v0/auth/confirm-email?token=" + token);
            context.setVariable("expiryMinutes", accountActivationTokenTtl);
            context.setVariable("supportEmail", supportEmail);

            String htmlContent = templateEngine.process("account-activate-email", context);

            sendHtmlEmail(to, "Подтверждение регистрации", htmlContent);
        } else {
            context.setVariable("username", username);
            context.setVariable("supportEmail", supportEmail);

            String htmlContent = templateEngine.process("welcome-email", context);

            sendHtmlEmail(to, "Добро пожаловать", htmlContent);
        }
    }

    public void sendResetPasswordEmail(String to, String token, String passwordResetTokenTtl) {
        Context context = new Context();
        context.setVariable("activationUrl", apiUrl + "/api/v0/auth/reset-password?token=" + token);
        context.setVariable("expiryMinutes", passwordResetTokenTtl);
        context.setVariable("supportEmail", supportEmail);

        String htmlContent = templateEngine.process("password-reset-email", context);

        sendHtmlEmail(to, "Сброс пароля", htmlContent);
    }

    public void sendAccountLockedEmail(String to, Instant time, String accountLockedCooldown) {
        Context context = new Context();
        context.setVariable("lockDate", " в " + time);
        context.setVariable("lockedTime", accountLockedCooldown);
        context.setVariable("supportEmail", supportEmail);

        String htmlContent = templateEngine.process("account-locked-email", context);

        sendHtmlEmail(to, "Аккаунт заморожен", htmlContent);
    }

    public void sendLoginEmail(String to, String ipAddress, String userAgent, Instant timestamp, String city) {
        Context context = new Context();
        context.setVariable("email", to);
        context.setVariable("ipAddress", ipAddress != null ? ipAddress : "неизвестен");
        context.setVariable("city", city);
        context.setVariable("userAgent", userAgent != null ? userAgent : "неизвестно");
        context.setVariable("loginDate", timestamp);
        context.setVariable("supportEmail", supportEmail);

        String htmlContent = templateEngine.process("login-email", context);

        sendHtmlEmail(to, "Новый вход в аккаунт", htmlContent);
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        long startedAt = System.currentTimeMillis();
        log.info("Sending email to {} (subject: '{}')", to, subject);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email sent to {} (subject: '{}') in {} ms", to, subject, System.currentTimeMillis() - startedAt);
        } catch (MessagingException e) {
            log.error("Failed to send email to {} (subject: '{}') after {} ms: {}",
                    to, subject, System.currentTimeMillis() - startedAt, e.getMessage(), e);
            throw new RuntimeException("Email sending failed for '" + subject + "' to " + to + ": " + e.getMessage(), e);
        }
    }
}