package com.alex.bank.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTOForClient {

    private Long id;

    private String username;

    private String email;

    private LocalDateTime createdAt;


}
