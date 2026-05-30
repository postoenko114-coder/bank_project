package com.alex.bank.controllers;

import com.alex.bank.dto.AuthenticationResponse;
import com.alex.bank.dto.user.UserDTO;
import com.alex.bank.dto.user.UserLogin;
import com.alex.bank.models.user.RoleUser;
import com.alex.bank.security.AuthenticationService;
import com.alex.bank.services.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Registration, login and current user endpoints")
public class AuthController {

    private final AuthenticationService authService;

    private final UserService userService;

    @Operation(summary = "Register a new user and return a JWT token")
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody UserDTO request) {
        if (userService.checkEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User with this email already exists");
        }
        String token = authService.register(request);
        return ResponseEntity.ok(Map.of("token", token));
    }

    @Operation(summary = "Authenticate a user and return a JWT token with target dashboard URL")
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@Valid @RequestBody UserLogin request) {
        UserDTO user = userService.findUserByEmail(request.getUsername());
        String targetUrl = "/dashboard.html";
        if (user.getRoleUser() == RoleUser.ADMIN) {
            targetUrl = "/admin.html";
        }
        return ResponseEntity.ok(new AuthenticationResponse(authService.authenticate(request.getUsername(), request.getPassword()), targetUrl));
    }

    @Operation(summary = "Get the currently authenticated user profile")
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(Authentication authentication) {
        return ResponseEntity.ok(userService.findUserByEmail(authentication.getName()));
    }
}
