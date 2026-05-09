package com.example.demo.controllers.client;

import com.example.demo.dto.user.UserDTO;
import com.example.demo.dto.user.UserDTOForClient;
import com.example.demo.models.user.User;
import com.example.demo.security.IsOwner;
import com.example.demo.services.user.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@IsOwner
@Validated
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}")
    public UserDTOForClient getUserProfile(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        return new UserDTOForClient(user.getId(), user.getRealUsername(), user.getEmail(), user.getCreatedAt());
    }

    @PutMapping("/{userId}")
    public UserDTOForClient editUserProfile(@PathVariable Long userId, @Valid @RequestBody UserDTO userDTO) {
        userService.updateUser(userId, userDTO);
        return new UserDTOForClient(userDTO.getId(), userDTO.getUsername(), userDTO.getEmail(), userDTO.getCreatedAt());
    }

    @PutMapping("/{userId}/changePassword")
    public UserDTO changePassword(@PathVariable Long userId, @RequestParam String oldPassword, @RequestParam String newPassword) {
        return userService.changePassword(userId, oldPassword, newPassword);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@NotNull @PathVariable Long userId) {
        userService.deleteUserById(userId);
    }


}
