package com.alex.bank.dto.user;

import com.alex.bank.models.user.RoleUser;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long id;

    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Email(message = "Invalid email format")
    private String email;

    private LocalDateTime createdAt;

    private RoleUser roleUser;

    private boolean hasPassword;
}
