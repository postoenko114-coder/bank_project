package com.alex.bank.controllers.client;

import com.alex.bank.dto.user.UserDTO;
import com.alex.bank.security.IsOwner;
import com.alex.bank.services.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@IsOwner
@Validated
@RequiredArgsConstructor
@Tag(name = "Client Users", description = "Client profile management endpoints")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get the authenticated user's profile")
    @GetMapping("/{userId}")
    public UserDTO getUserProfile(@PathVariable Long userId) {
        return userService.getUserById(userId);
    }

    @Operation(summary = "Update the authenticated user's profile")
    @PutMapping("/{userId}")
    public UserDTO editUserProfile(@PathVariable Long userId, @Valid @RequestBody UserDTO userDTO) {
        return userService.updateUser(userId, userDTO);
    }

    @Operation(summary = "Change the authenticated user's password")
    @PutMapping("/{userId}/changePassword")
    public UserDTO changePassword(@PathVariable Long userId, @RequestParam String oldPassword, @RequestParam String newPassword) {
        return userService.changePassword(userId, oldPassword, newPassword);
    }

    @Operation(summary = "Delete the authenticated user's profile")
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@NotNull @PathVariable Long userId) {
        userService.deleteUserById(userId);
    }


}
