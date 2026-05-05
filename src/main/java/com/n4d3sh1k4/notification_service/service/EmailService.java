package com.n4d3sh1k4.notification_service.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.UnsupportedEncodingException;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public void sendRegistrationEmail(String to, String username, String token) {
        Context context = new Context();
        if(token != null) {
            context.setVariable("supportEmail", "team@reshala.n4d3sh1k4.site");
            context.setVariable("expiryMinutes", 15);
            context.setVariable("username", username);
            context.setVariable("activationUrl", "https://api.reshala.n4d3sh1k4.site/api/v0/auth/confirm-email?token=" + token);
            String htmlContent = templateEngine.process("account-activate-email", context);
            sendHtmlEmail(to, "Подтверждение регистрации", htmlContent);
        } else {
            context.setVariable("supportEmail", "team@reshala.n4d3sh1k4.site");
            context.setVariable("username", username);
            String htmlContent = templateEngine.process("welcome-email", context);
            sendHtmlEmail(to, "Добро пожаловать", htmlContent);
        }
        log.info("Email sent to {}", to);
    }

    public void sendResetPasswordEmail(String to, String token) {
        Context context = new Context();
        context.setVariable("supportEmail", "team@reshala.n4d3sh1k4.site");
        context.setVariable("expiryMinutes", 15);
        context.setVariable("activationUrl", "https://api.reshala.n4d3sh1k4.site/api/v0/auth/reset-password?token=" + token);

        String htmlContent = templateEngine.process("password-reset-email", context);

        sendHtmlEmail(to, "Сброс пароля", htmlContent);
        log.info("Reset password email sent to {}", to);
    }

    public void sendAccountLockedEmail(String to, Instant time) {
        Context context = new Context();
        context.setVariable("supportEmail", "team@reshala.n4d3sh1k4.site");
        context.setVariable("lockDate", " в "+time);

        String htmlContent = templateEngine.process("account-locked-email", context);

        sendHtmlEmail(to, "Аккаунт заморожен", htmlContent);
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("accounts@reshala.n4d3sh1k4.site", "Reshala Accounts");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}