package com.example.demo.controllers.admin;

import com.example.demo.dto.user.UserCreateDTO;
import com.example.demo.dto.user.UserDTO;
import com.example.demo.dto.user.UserDTOAdmin;
import com.example.demo.models.user.User;
import com.example.demo.security.IsOwner;
import com.example.demo.services.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@IsOwner
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserDTOAdmin> getUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{userId}")
    public UserDTO getUser(@PathVariable Long userId) {
        return userService.getUserById(userId).toDTO();
    }

    @GetMapping("/search")
    public List<UserDTOAdmin> searchUser(@RequestParam String query){
        return userService.searchUsers(query);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTOAdmin createUser(@RequestBody UserCreateDTO createDTO) {
        return userService.createUser(createDTO);
    }

    @PutMapping("/{userId}")
    public UserDTOAdmin updateUser(@PathVariable Long userId, @RequestBody UserDTO userDTO) {
        userService.updateUser(userId, userDTO);
        User user = userService.getUserById(userId);
        return new UserDTOAdmin(userId, user.getUsername(),user.getEmail(), user.getRoleUser(), user.getCreatedAt());
    }

    @PutMapping("/{userId}/changeRoleUser")
    public UserDTOAdmin changeRoleUser(@PathVariable Long userId, @RequestParam String role) {
        return userService.changeRole(userId, role);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        userService.deleteUserById(userId);
    }

}
