package com.example.demo.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCreateDTO {

    private String username;

    private String email;

    private String password;

    private String roleUser;

}
