package com.mo.mediaodyssey.auth.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mo.mediaodyssey.auth.dto.UserDto;
import com.mo.mediaodyssey.auth.model.User;
import com.mo.mediaodyssey.auth.repository.UserRepository;

@Service
public class AuthService {

    @Autowired
    private VerificationService verificationService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public void loginUser(UserDto dto) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(dto.email(), dto.password()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Transactional
    public void registerUser(UserDto dto) {
        Boolean userExists = userRepository.existsByEmail(dto.email());

        if (userExists) {
            throw new BadCredentialsException("User with email address " + dto.email() + " already exists.");
        }
        User user = new User(dto.email(), passwordEncoder.encode(dto.password()));
        userRepository.save(user);
        verificationService.createVerification(user);
    }
}
