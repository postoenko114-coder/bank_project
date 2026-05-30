package com.alex.bank.dto.support;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupportDTO {

    private Long id;

    private String subject;

    private String message;

    private String userEmail;

    private LocalDateTime createdAt;

}
