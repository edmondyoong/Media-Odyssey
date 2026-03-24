package com.mo.mediaodyssey.auth.services;

import java.util.Date;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.mo.mediaodyssey.auth.model.VerificationToken;
import com.mo.mediaodyssey.auth.repository.UserRepository;
import com.mo.mediaodyssey.auth.repository.VerificationTokenRepository;
import com.mo.mediaodyssey.auth.dto.ResendVerifyTokenDto;
import com.mo.mediaodyssey.auth.dto.VerifyTokenDto;
import com.mo.mediaodyssey.auth.exception.InvalidVerificationTokenException;
import com.mo.mediaodyssey.auth.exception.UserAlreadyVerifiedException;
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

        // Build verification token email
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .build()
                .toUriString();
        String to = user.getEmail();
        String subject = "Confirm Registration to " + this.appName;
        String message = "Confirm registration by clicking this link: " + baseUrl + "/api/auth/verify?token=" + token;

        // Send verification token email
        emailService.sendEmail(to, subject, message);

        // Save verification token
        verificationTokenRepository.save(vt);
    }

    @Transactional
    public void verifyUser(VerifyTokenDto dto) {
        try {
            // Find the User if the token is valid and not expired.
            String token = dto.token();
            VerificationToken tokenEntity = verificationTokenRepository.findByToken(token)
                    .filter(item -> item.getExpiryDate().after(new Date())).orElseThrow();
            User user = tokenEntity.getUser();

            // Enable the user and save this change.
            user.setEnabled(true);
            userRepository.save(user);

            // Delete the verification token because it is no longer needed.
            user.setVerificationToken(null);
            verificationTokenRepository.delete(tokenEntity);
        } catch (NoSuchElementException e) {
            throw new InvalidVerificationTokenException("Verification token is invalid. Please try again.");
        }
    }

    @Transactional
    public void resendVerification(ResendVerifyTokenDto dto) {
        try {
            User user = userRepository.findByEmail(dto.email()).orElseThrow();
            if (user.isAccountNonLocked() == false) {
                throw new LockedException("User is locked. Cannot verify locked user. Contact support.");
            } else if (user.isEnabled()) { // If User is already email verified.
                throw new UserAlreadyVerifiedException("User is already verified. Please log in.");
            } else {
                VerificationToken tokenEntity = user.getVerificationToken();
                if (tokenEntity != null) { // If previous token exist, delete it. If not, skip deleting.
                    user.setVerificationToken(null);
                    verificationTokenRepository.delete(tokenEntity);
                    verificationTokenRepository.flush();
                }
                this.createVerification(user);
            }
        } catch (NoSuchElementException e) {
            throw new UsernameNotFoundException("User not found with email: " + dto.email());
        }

    }
}
