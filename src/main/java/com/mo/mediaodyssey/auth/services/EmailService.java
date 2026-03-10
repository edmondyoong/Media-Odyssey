package com.mo.mediaodyssey.auth.services;

import org.springframework.stereotype.Service;
import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;

import jakarta.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.DisabledException;

@Service
public class EmailService {
    // Inspiried by
    // https://github.com/resend/resend-examples/blob/main/java-resend-examples/spring_boot_app/src/main/java/com/resend/springboot/EmailController.java
    // Debugging assisted by AI.

    private final Resend resend;
    private final String from;

    public EmailService(
            @Value("${RESEND_API_KEY}") String apiKey,
            @Value("${EMAIL_FROM}") String from) {
        this.resend = new Resend(apiKey);
        this.from = from;
    }

    public void sendEmail(@NotBlank String to, @NotBlank String subject, @NotBlank String message) {
        try {
            var params = CreateEmailOptions.builder()
                    .from(from)
                    .to(to)
                    .subject(subject)
                    .html("<p>" + message + "</p>")
                    .build();

            resend.emails().send(params);
        } catch (Exception e) {
            throw new DisabledException("Unable to send email");
        }
    }
}
