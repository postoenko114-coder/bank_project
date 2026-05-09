package com.example.demo.controllers;

import com.example.demo.dto.AuthenticationResponse;
import com.example.demo.dto.user.UserDTO;
import com.example.demo.dto.user.UserLogin;
import com.example.demo.models.user.RoleUser;
import com.example.demo.security.AuthenticationService;
import com.example.demo.services.user.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService authService;

    private final UserService userService;

    public AuthController(AuthenticationService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody UserDTO request) {
        if (userService.checkEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User with this email already exists");
        }
        String token = authService.register(request);
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@Valid @RequestBody UserLogin request) {
        var user = userService.findUserByEmail(request.getUsername());
        String targetUrl = "/dashboard.html";
        if (user.getRoleUser() == RoleUser.ADMIN) {
            targetUrl = "/admin.html";
        }
        return ResponseEntity.ok(new AuthenticationResponse(authService.authenticate(request.getUsername(), request.getPassword()), targetUrl));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(Authentication authentication) {
        UserDTO userDto = userService.findUserByEmail(authentication.getName()).toDTO();
        return ResponseEntity.ok(userDto);
    }
}
