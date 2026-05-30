package com.alex.bank.controllers.admin;

import com.alex.bank.dto.user.UserCreateDTO;
import com.alex.bank.dto.user.UserDTO;
import com.alex.bank.dto.user.UserDTOAdmin;
import com.alex.bank.mapper.UserMapper;
import com.alex.bank.services.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin Users", description = "Administrative user management endpoints")
public class AdminUserController {

    private final UserService userService;

    private final UserMapper userMapper;

    @Operation(summary = "Get all users as admin")
    @GetMapping
    public List<UserDTOAdmin> getUsers() {
        return userService.getAllUsers();
    }

    @Operation(summary = "Get a user by id as admin")
    @GetMapping("/{userId}")
    public UserDTO getUser(@PathVariable Long userId) {
        return userService.getUserById(userId);
    }

    @Operation(summary = "Search users by id, username or email as admin")
    @GetMapping("/search")
    public List<UserDTOAdmin> searchUser(@RequestParam String query) {
        return userService.searchUsers(query);
    }

    @Operation(summary = "Create a user as admin")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTOAdmin createUser(@Valid @RequestBody UserCreateDTO createDTO) {
        return userService.createUser(createDTO);
    }

    @Operation(summary = "Update a user profile as admin")
    @PutMapping("/{userId}")
    public UserDTOAdmin updateUser(@PathVariable Long userId, @Valid @RequestBody UserDTO userDTO) {
        userService.updateUser(userId, userDTO);
        UserDTO updatedUser = userService.getUserById(userId);
        return userMapper.toDTOAdminFromDTO(updatedUser);
    }

    @Operation(summary = "Change a user role as admin")
    @PutMapping("/{userId}/changeRoleUser")
    public UserDTOAdmin changeRoleUser(@PathVariable Long userId, @RequestParam String role) {
        return userService.changeRole(userId, role);
    }

    @Operation(summary = "Delete a user as admin")
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        userService.deleteUserById(userId);
    }

}
