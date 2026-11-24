package com.dulcepack.backend.identity.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "true")
public class SmtpPasswordResetNotifier implements PasswordResetNotifier {
    private final JavaMailSender mailSender;
    private final MailProperties properties;

    public SmtpPasswordResetNotifier(JavaMailSender mailSender, MailProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    @Override
    public void send(String email, String rawToken) {
        String separator = properties.resetBaseUrl().contains("?") ? "&" : "?";
        String resetUrl = properties.resetBaseUrl() + separator + "token="
                + URLEncoder.encode(rawToken, StandardCharsets.UTF_8);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.from());
        message.setTo(email);
        message.setSubject("Restablece tu contraseña de DulcePack");
        message.setText("Usa este enlace para restablecer tu contraseña: " + resetUrl
                + "\n\nSi no solicitaste el cambio, ignora este mensaje.");
        mailSender.send(message);
    }
}
