package com.alex.bank.unit.controller.admin;

import com.alex.bank.controllers.admin.AdminUserController;
import com.alex.bank.dto.user.UserCreateDTO;
import com.alex.bank.dto.user.UserDTO;
import com.alex.bank.dto.user.UserDTOAdmin;
import com.alex.bank.exception.GlobalExceptionHandler;
import com.alex.bank.mapper.UserMapperImpl;
import com.alex.bank.models.user.RoleUser;
import com.alex.bank.security.JwtService;
import com.alex.bank.services.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AdminUserController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
@Import(UserMapperImpl.class)
public class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUsers_ShouldReturnUserDTOAdminList() throws Exception {
        UserDTOAdmin u1 = new UserDTOAdmin(1L, "alex", "1111111111", "alex@gmail.com", LocalDateTime.now(), RoleUser.CLIENT);
        UserDTOAdmin u2 = new UserDTOAdmin(2L, "maria", "maria@gmail.com", "111111111", LocalDateTime.now(), RoleUser.CLIENT);

        when(userService.getAllUsers()).thenReturn(List.of(u1, u2));

        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(userService).getAllUsers();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUser_ShouldReturnUserDTO_WhenUserExists() throws Exception {
        UserDTO fakeUser = new UserDTO();
        fakeUser.setId(1L);
        fakeUser.setUsername("alex");
        fakeUser.setEmail("alex@gmail.com");

        when(userService.getUserById(1L)).thenReturn(fakeUser);

        mockMvc.perform(get("/api/v1/admin/users/{userId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alex"))
                .andExpect(jsonPath("$.email").value("alex@gmail.com"));

        verify(userService).getUserById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUser_ShouldReturn404_WhenUserDoesNotExist() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"))
                .when(userService).getUserById(99L);

        mockMvc.perform(get("/api/v1/admin/users/{userId}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchUser_ShouldReturnMatchingUsers() throws Exception {
        UserDTOAdmin u1 = new UserDTOAdmin(1L, "alex", "alex@gmail.com", "111111111",LocalDateTime.now(), RoleUser.CLIENT);

        when(userService.searchUsers("alex")).thenReturn(List.of(u1));

        mockMvc.perform(get("/api/v1/admin/users/search").param("query", "alex"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username").value("alex"));

        verify(userService).searchUsers("alex");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_ShouldReturnUserDTOAdmin_WhenDataIsValid() throws Exception {
        UserCreateDTO createDTO = new UserCreateDTO("alex", "alex@gmail.com", "password123", "CLIENT");

        UserDTOAdmin fakeResult = new UserDTOAdmin(1L, "alex", "alex@gmail.com", "1111111111", LocalDateTime.now(), RoleUser.CLIENT);

        when(userService.createUser(any(UserCreateDTO.class))).thenReturn(fakeResult);

        mockMvc.perform(post("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("alex"));

        verify(userService).createUser(any(UserCreateDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_ShouldReturn409_WhenUsernameAlreadyExists() throws Exception {
        UserCreateDTO createDTO = new UserCreateDTO("alex", "alex@gmail.com", "password123", "CLIENT");

        doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists"))
                .when(userService).createUser(any(UserCreateDTO.class));

        mockMvc.perform(post("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_ShouldReturnUpdatedUserDTOAdmin_WhenUserExists() throws Exception {
        UserDTO fakeDTO = new UserDTO();
        fakeDTO.setUsername("newName");
        fakeDTO.setEmail("new@gmail.com");

        UserDTO fakeUser = new UserDTO();
        fakeUser.setId(1L);
        fakeUser.setUsername("newName");
        fakeUser.setEmail("new@gmail.com");
        fakeUser.setRoleUser(RoleUser.CLIENT);

        when(userService.updateUser(eq(1L), any(UserDTO.class))).thenReturn(fakeDTO);
        when(userService.getUserById(1L)).thenReturn(fakeUser);

        mockMvc.perform(put("/api/v1/admin/users/{userId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fakeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newName"));

        verify(userService).updateUser(eq(1L), any(UserDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void changeRoleUser_ShouldReturnUserDTOAdmin_WhenUserExists() throws Exception {
        UserDTOAdmin fakeResult = new UserDTOAdmin(1L, "alex", "alex@gmail.com", "111111111", LocalDateTime.now(), RoleUser.ADMIN);

        when(userService.changeRole(1L, "admin")).thenReturn(fakeResult);

        mockMvc.perform(put("/api/v1/admin/users/{userId}/changeRoleUser", 1L)
                        .param("role", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleUser").value("ADMIN"));

        verify(userService).changeRole(1L, "admin");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_ShouldReturn204_WhenUserExists() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/users/{userId}", 1L))
                .andExpect(status().isNoContent());

        verify(userService).deleteUserById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_ShouldReturn404_WhenUserDoesNotExist() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"))
                .when(userService).deleteUserById(99L);

        mockMvc.perform(delete("/api/v1/admin/users/{userId}", 99L))
                .andExpect(status().isNotFound());
    }
}
