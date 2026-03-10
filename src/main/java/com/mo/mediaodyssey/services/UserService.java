package com.mo.mediaodyssey.services;

import com.mo.mediaodyssey.auth.model.User;
import com.mo.mediaodyssey.auth.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void registerUser(String username, String email, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalStateException("Username already exists");
        }
        // Note: password stored as plain text here — auth system uses BCrypt separately
        User user = new User(email, username, password, true, true, "ROLE_USER");
        userRepository.save(user);
    }

    public User loginUser(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Invalid username or password"));

        if (!user.getPassword().equals(password)) {
            throw new IllegalStateException("Invalid username or password");
        }

        return user;
    }

    public String getUsernameById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("User not found"))
                .getUsername();
    }
}