package com.example.demo.security;

import com.example.demo.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("userSecurity")
public class UserSecurity {

    private final UserRepository userRepository;

    public UserSecurity(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean isResourceOwner(Authentication authentication, Long userId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        String currentEmail = authentication.getName();

        return userRepository.findByEmail(currentEmail)
                .map(user -> user.getId().equals(userId))
                .orElse(false);
    }
}
