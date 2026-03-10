package com.mo.mediaodyssey.auth.services;

import java.util.Date;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.mo.mediaodyssey.auth.model.VerificationToken;
import com.mo.mediaodyssey.auth.repository.UserRepository;
import com.mo.mediaodyssey.auth.repository.VerificationTokenRepository;
import com.mo.mediaodyssey.auth.dto.ResendVerifyTokenDto;
import com.mo.mediaodyssey.auth.dto.VerifyTokenDto;
import com.mo.mediaodyssey.auth.model.User;

@Service
public class VerificationService {

    // Inspiried by: https://www.baeldung.com/registration-verify-user-by-email
    // Debugging assisted by AI

    // Expiry length determined by environment variable. Default: 60 minutes
    private final int tokenExpiryInMinutes;
    private final VerificationTokenRepository verificationTokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${APP_NAME:App}")
    private String appName;

    public VerificationService(VerificationTokenRepository verificationTokenRepository,
            @Value("${VERIFY_TOKEN_EXPIRY_IN_MINUTES:60}") int tokenExpiryInMinutes, UserRepository userRepository,
            EmailService emailService) {
        this.tokenExpiryInMinutes = tokenExpiryInMinutes;
        this.verificationTokenRepository = verificationTokenRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Transactional
    public void createVerification(User user) {
        // Create verification token
        String token = UUID.randomUUID().toString();
        VerificationToken vt = new VerificationToken(token, user, tokenExpiryInMinutes);

        // Save verification token
        verificationTokenRepository.save(vt);

        // Build verification token email
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .build()
                .toUriString();
        String to = user.getEmail();
        String subject = "Confirm Registration to " + this.appName;
        String message = "Confirm registration by clicking this link: " + baseUrl + "/api/auth/verify?token=" + token;

        // Send verification token email
        emailService.sendEmail(to, subject, message);
    }

    @Transactional
    public Boolean verifyUser(VerifyTokenDto dto) {
        String token = dto.token();
        return verificationTokenRepository.findByToken(token)
                .filter(item -> item.getExpiryDate().after(new Date()))
                .map(item -> {
                    User user = item.getUser();
                    user.setEnabled(true);
                    userRepository.save(user);
                    verificationTokenRepository.delete(item);
                    return true;
                }).orElse(false);
    }

    @Transactional
    public void resendVerification(ResendVerifyTokenDto dto) {
        try {
            User user = userRepository.findByEmail(dto.email()).orElseThrow();
            if (user.isEnabled()) { // If User is already email verified.
                throw new BadCredentialsException("User is already verified.");
            }
            createVerification(user);
        } catch (NoSuchElementException e) {
            throw new BadCredentialsException("User does not exist.");
        }

    }
}
