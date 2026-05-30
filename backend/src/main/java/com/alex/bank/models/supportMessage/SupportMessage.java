package com.alex.bank.models.supportMessage;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "supportMessages")
public class SupportMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String subject;

    private String message;

    private String userEmail;

    @Enumerated(EnumType.STRING)
    private StatusSupportMessage statusSupportMessage;

    private LocalDateTime createdAt;

}
