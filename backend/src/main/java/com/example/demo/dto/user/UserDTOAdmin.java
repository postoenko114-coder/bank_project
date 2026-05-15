package com.example.demo.dto.user;

import com.example.demo.models.user.RoleUser;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTOAdmin {

    private Long id;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 30, message = "Min quantity of signs is 8, max is 30")
    private String password;

    @NotBlank(message = "Email is required")
    @Email
    private String email;

    private LocalDateTime createdAt;

    private RoleUser roleUser;


}
