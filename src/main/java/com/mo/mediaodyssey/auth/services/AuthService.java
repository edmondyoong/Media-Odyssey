package com.mo.mediaodyssey.auth.services;

import com.mo.mediaodyssey.layout.services.AvatarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mo.mediaodyssey.auth.dto.UserDto;
import com.mo.mediaodyssey.auth.model.User;
import com.mo.mediaodyssey.auth.repository.UserRepository;

@Service
public class AuthService {

    private final AvatarService avatarService;

    @Autowired
    private VerificationService verificationService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    AuthService(AvatarService avatarService) {
        this.avatarService = avatarService;
    }

    @Transactional
    public Authentication loginUser(UserDto dto) {
        return authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(dto.email(), dto.password()));
    }

    @Transactional
    public void registerUser(UserDto dto) {
        Boolean userExists = userRepository.existsByEmail(dto.email());

        if (userExists) {
            throw new BadCredentialsException("User with email address " + dto.email() + " already exists.");
        }
        User user = new User(dto.email(), passwordEncoder.encode(dto.password()));

        /* Added generated avatar for new users */
        if (user.getAvatar_path()==null || user.getAvatar_path().isEmpty()) {
            user.setAvatar_path(avatarService.avatarGenerate(user.getId()));
        }

        userRepository.save(user);
        verificationService.createVerification(user);
    }
}
