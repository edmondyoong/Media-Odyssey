package com.mo.mediaodyssey.auth.services;

import com.mo.mediaodyssey.auth.dto.UserDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.BadCredentialsException;

import com.mo.mediaodyssey.auth.model.User;
import com.mo.mediaodyssey.auth.repository.UserRepository;

public class AuthAdminService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public void createAdminUser(UserDto dto) {
        // Check if a User with this email address already exist.
        Boolean userExists = userRepository.existsByEmail(dto.email());

        if (userExists) {
            throw new BadCredentialsException("User with email address " + dto.email() + " already exists.");
        }

        // Create the User
        User user = new User(dto.email(), dto.email(), passwordEncoder.encode(dto.password()), true, true,
                "ROLE_ADMIN");

        // Save the user
        userRepository.save(user);
    }
}
