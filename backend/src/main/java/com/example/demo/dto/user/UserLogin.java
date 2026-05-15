package com.example.demo.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLogin {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 30, message = "Min quantity of signs is 8, max is 30")
    private String password;

}
