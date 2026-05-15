package com.example.demo.dto.user;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDTOForClient {

    private Long id;

    private String username;

    private String email;

    private LocalDateTime createdAt;


}
