package com.example.demo.unit.controller;

import com.example.demo.controllers.AuthController;
import com.example.demo.dto.user.UserDTO;
import com.example.demo.dto.user.UserLogin;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.mapper.UserMapperImpl;
import com.example.demo.models.user.RoleUser;
import com.example.demo.models.user.User;
import com.example.demo.security.AuthenticationService;
import com.example.demo.security.JwtService;
import com.example.demo.services.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AuthController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
@Import(UserMapperImpl.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationService authService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @Test
    void register_ShouldReturn200AndToken_WhenDataIsValid() throws Exception {
        UserDTO requestDto = new UserDTO();
        requestDto.setEmail("newuser@gmail.com");
        requestDto.setUsername("newUser");
        requestDto.setPassword("ValidPassword123");

        when(userService.checkEmail(requestDto.getEmail())).thenReturn(false);
        when(authService.register(any(UserDTO.class))).thenReturn("fake-jwt-token");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"));
    }

    @Test
    void register_ShouldReturn409Conflict_WhenEmailAlreadyExists() throws Exception {
        UserDTO requestDto = new UserDTO();
        requestDto.setEmail("exist@gmail.com");
        requestDto.setUsername("user");
        requestDto.setPassword("ValidPassword123");

        when(userService.checkEmail(requestDto.getEmail())).thenReturn(true);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                .andExpect(status().isConflict());
    }

    @Test
    void authenticate_ShouldReturnDashboardUrl_WhenUserIsNormal() throws Exception {
        UserLogin loginReq = new UserLogin("user@gmail.com", "Password123");

        User fakeUser = new User();
        fakeUser.setRoleUser(RoleUser.CLIENT);

        when(userService.findUserByEmail(loginReq.getUsername())).thenReturn(fakeUser);
        when(authService.authenticate(loginReq.getUsername(), loginReq.getPassword())).thenReturn("fake-token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetUrl").value("/dashboard.html"));
    }

    @Test
    void authenticate_ShouldReturnAdminUrl_WhenUserIsAdmin() throws Exception {
        UserLogin loginReq = new UserLogin("admin@gmail.com", "Password123");

        User fakeAdmin = new User();
        fakeAdmin.setRoleUser(RoleUser.ADMIN); // Админ

        when(userService.findUserByEmail(loginReq.getUsername())).thenReturn(fakeAdmin);
        when(authService.authenticate(loginReq.getUsername(), loginReq.getPassword())).thenReturn("fake-token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetUrl").value("/admin.html"));
    }

    @Test
    void authenticate_ShouldReturn400_WhenValidationFails() throws Exception {
        UserLogin badReq = new UserLogin("test@gmail.com", "123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badReq)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).findUserByEmail(anyString());
    }


    @Test
    void getCurrentUser_ShouldReturnUserDTO() throws Exception {
        User fakeUser = new User();
        fakeUser.setEmail("current@gmail.com");
        fakeUser.setUsername("current");

        when(userService.findUserByEmail("current@gmail.com")).thenReturn(fakeUser);

        var fakeAuth = new UsernamePasswordAuthenticationToken("current@gmail.com", "password");

        mockMvc.perform(get("/api/auth/me")
                        .principal(fakeAuth))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("current@gmail.com"));
    }
}