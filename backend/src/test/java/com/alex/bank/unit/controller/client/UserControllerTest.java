package com.alex.bank.unit.controller.client;


import com.alex.bank.controllers.client.UserController;
import com.alex.bank.dto.user.UserDTO;
import com.alex.bank.exception.GlobalExceptionHandler;
import com.alex.bank.security.JwtService;
import com.alex.bank.services.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = {UserController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void getUserProfile_ShouldReturnUserDTOForClient() throws Exception {
        UserDTO fakeUser = new UserDTO();
        fakeUser.setId(1L);
        fakeUser.setUsername("fakeUser");
        fakeUser.setEmail("test@gmail.com");

        when(userService.getUserById(fakeUser.getId())).thenReturn(fakeUser);

        mockMvc.perform(get("/api/v1/users/{userId}", fakeUser.getId())
                        .contentType(MediaType.APPLICATION_JSON))


                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(fakeUser.getId()))
                .andExpect(jsonPath("$.username").value("fakeUser"))
                .andExpect(jsonPath("$.email").value("test@gmail.com"));

        verify(userService).getUserById(fakeUser.getId());

    }

    @Test
    @WithMockUser
    void editUserProfile_ShouldReturnUserDTOForClient() throws Exception {
        UserDTO fakeUser = new UserDTO();
        fakeUser.setId(1L);
        fakeUser.setUsername("fakeUser");
        fakeUser.setEmail("test@gmail.com");
        fakeUser.setCreatedAt(LocalDateTime.now());
        fakeUser.setPassword("password");


        when(userService.updateUser(eq(fakeUser.getId()), any(UserDTO.class))).thenReturn(fakeUser);

        mockMvc.perform(put("/api/v1/users/{userId}", fakeUser.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(fakeUser)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(fakeUser.getId()))
                .andExpect(jsonPath("$.username").value("fakeUser"))
                .andExpect(jsonPath("$.email").value("test@gmail.com"));

        verify(userService).updateUser(eq(fakeUser.getId()), any(UserDTO.class));
    }

    @Test
    @WithMockUser
    void editUserProfile_ShouldReturn404_WhenUserNotFound() throws Exception {
        UserDTO fakeUser = new UserDTO();
        fakeUser.setId(99L);
        fakeUser.setUsername("fake");
        fakeUser.setEmail("test@gmail.com");
        fakeUser.setPassword("Password");
        fakeUser.setCreatedAt(LocalDateTime.now());

        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"))
                .when(userService).updateUser(eq(99L), any(UserDTO.class));

        mockMvc.perform(put("/api/v1/users/{userId}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fakeUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void editUserProfile_ShouldReturn409_WhenEmailAlreadyTaken() throws Exception {
        UserDTO fakeUser = new UserDTO();
        fakeUser.setId(1L);
        fakeUser.setUsername("alex");
        fakeUser.setEmail("taken@gmail.com");
        fakeUser.setPassword("Password");
        fakeUser.setCreatedAt(LocalDateTime.now());

        doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Email is already taken"))
                .when(userService).updateUser(eq(1L), any(UserDTO.class));

        mockMvc.perform(put("/api/v1/users/{userId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fakeUser)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    void changePassword_ShouldReturnUserDTO() throws Exception {
        UserDTO fakeUser = new UserDTO();
        fakeUser.setId(1L);
        fakeUser.setUsername("fakeUser");
        fakeUser.setEmail("test@gmail.com");
        fakeUser.setCreatedAt(LocalDateTime.now());
        fakeUser.setPassword("newPassword");

        String oldPassword = "oldPassword";
        String newPassword = "newPassword";

        when(userService.changePassword(fakeUser.getId(), oldPassword, newPassword))
                .thenReturn(fakeUser);

        mockMvc.perform(put("/api/v1/users/{userId}/changePassword", fakeUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .param("oldPassword", oldPassword)
                .param("newPassword", newPassword))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@gmail.com"));

        verify(userService).changePassword(fakeUser.getId(), oldPassword, newPassword);
    }

    @Test
    @WithMockUser
    void changePassword_ShouldReturn400_WhenOldPasswordIsMissing() throws Exception {
        mockMvc.perform(put("/api/v1/users/1/changePassword")
                        .param("newPassword", "123456"))

                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void changePassword_ShouldReturn403_WhenOldPasswordDoesNotMatch() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Old Password not match"))
                .when(userService).changePassword(1L, "wrongOld", "newPassword");

        mockMvc.perform(put("/api/v1/users/{userId}/changePassword", 1L)
                        .param("oldPassword", "wrongOld")
                        .param("newPassword", "newPassword"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void changePassword_ShouldReturn404_WhenUserNotFound() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"))
                .when(userService).changePassword(99L, "oldPass", "newPass");

        mockMvc.perform(put("/api/v1/users/{userId}/changePassword", 99L)
                        .param("oldPassword", "oldPass")
                        .param("newPassword", "newPass"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void deleteUser_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/users/{userId}", 1L))
                .andExpect(status().isNoContent());

        verify(userService).deleteUserById(1L);
    }

    @Test
    @WithMockUser
    void deleteUser_ShouldReturn404_WhenUserNotFound() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"))
                .when(userService).deleteUserById(999L);

        mockMvc.perform(delete("/api/v1/users/{userId}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getUserProfile_ShouldReturn404_WhenUserNotFound() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"))
                .when(userService).getUserById(99L);

        mockMvc.perform(get("/api/v1/users/{userId}", 99L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
