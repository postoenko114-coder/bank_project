package com.example.demo.models.supportMessage;

import com.example.demo.dto.support.SupportDTO;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "supportMessages")
public class SupportMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String subject;

    private String message;

    private String userEmail;

    private StatusSupportMessage statusSupportMessage;

    private LocalDateTime createdAt;

    public SupportMessage() {}

    public SupportMessage(String subject, String message, String userEmail, StatusSupportMessage statusSupportMessage) {
        this.subject = subject;
        this.message = message;
        this.userEmail = userEmail;
        this.statusSupportMessage = statusSupportMessage;
        this.createdAt = LocalDateTime.now();
    }

    public SupportDTO toDTO() {
        SupportDTO supportDTO = new SupportDTO();
        supportDTO.setId(id);
        supportDTO.setSubject(subject);
        supportDTO.setMessage(message);
        supportDTO.setUserEmail(userEmail);
        supportDTO.setCreatedAt(createdAt);
        return supportDTO;
    }

    public LocalDateTime getCreatedAt() {return createdAt;}

    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}

    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}

    public String getMessage() {return message;}

    public void setMessage(String message) {this.message = message;}

    public StatusSupportMessage getStatusSupportMessage() {return statusSupportMessage;}

    public void setStatusSupportMessage(StatusSupportMessage statusSupportMessage) {this.statusSupportMessage = statusSupportMessage;}

    public String getSubject() {return subject;}

    public void setSubject(String subject) {this.subject = subject;}

    public String getUserEmail() {return userEmail;}

    public void setUserEmail(String userEmail) {this.userEmail = userEmail;}
}
