package com.example.demo.dto.support;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SupportDTO {

    private Long id;

    private String subject;

    private String message;

    private String userEmail;

    private LocalDateTime createdAt;

}
