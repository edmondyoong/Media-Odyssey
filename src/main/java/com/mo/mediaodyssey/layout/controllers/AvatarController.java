package com.mo.mediaodyssey.layout.controllers;

import com.mo.mediaodyssey.auth.repository.UserRepository;
import com.mo.mediaodyssey.shared.model.User;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class AvatarController {
    
    /* This controller is for handling avatar related requests
        * Take Request of avatar type change from front end and update in the database.
        * Store it so the application knows which avatar is chosen to use
        * Notice: the avatar type is either "custom" or "default.
    */ 

    private final UserRepository userRepository;

    AvatarController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/user/profile/avatar/selectedType")
    public ResponseEntity<?> selectedAvatarType(@RequestBody Map<String, String> body,
                                                 Authentication authentication) {
        String selectedAvatarType = body.get("selectedAvatarType");

        User user = (User) authentication.getPrincipal();

        user.setSelected_avatar_type(selectedAvatarType);

        userRepository.save(user);

        return ResponseEntity.ok().build();
    }
}
