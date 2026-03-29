package com.mo.mediaodyssey.auth.services;

import com.mo.mediaodyssey.auth.dto.UserDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.BadCredentialsException;

import com.mo.mediaodyssey.auth.model.User;
import com.mo.mediaodyssey.auth.repository.UserRepository;
import com.mo.mediaodyssey.layout.services.AvatarService;

@Service
public class AuthAdminService {

    private final AvatarService avatarService = new AvatarService(); 
    
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
                "ROLE_ADMIN", null);
        
        /* Added generated avatar for new users */
        if (user.getAvatar_path()==null || user.getAvatar_path().isEmpty()) {
            user.setAvatar_path(avatarService.avatarGenerate(user.getId()));
        }

        // Save the user
        userRepository.save(user);
    }
}
