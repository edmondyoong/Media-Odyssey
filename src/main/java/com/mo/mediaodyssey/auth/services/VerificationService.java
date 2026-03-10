package com.mo.mediaodyssey.auth.services;

import java.util.Date;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public VerificationService(VerificationTokenRepository verificationTokenRepository,
            @Value("${VERIFY_TOKEN_EXPIRY_IN_MINUTES:60}") int tokenExpiryInMinutes, UserRepository userRepository) {
        this.tokenExpiryInMinutes = tokenExpiryInMinutes;
        this.verificationTokenRepository = verificationTokenRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void createVerification(User user) {
        String token = UUID.randomUUID().toString();
        VerificationToken vt = new VerificationToken(token, user, tokenExpiryInMinutes);
        verificationTokenRepository.save(vt);

        // TODO: send email with token via Resend
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
