package com.example.demo.controllers.admin;

import com.example.demo.dto.support.SupportDTO;
import com.example.demo.dto.support.SupportReplyDTO;
import com.example.demo.models.notification.TypeNotification;
import com.example.demo.models.user.User;
import com.example.demo.services.EmailService;
import com.example.demo.services.notification.NotificationService;
import com.example.demo.services.supportMessage.SupportMessageService;
import com.example.demo.services.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/supportMessages")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSupportMessageController {

    private final SupportMessageService supportMessageService;

    private final UserService userService;

    private final NotificationService notificationService;

    private final EmailService emailService;


    public AdminSupportMessageController(SupportMessageService supportMessageService, UserService userService, NotificationService notificationService, EmailService emailService) {
        this.supportMessageService = supportMessageService;
        this.userService = userService;
        this.notificationService = notificationService;
        this.emailService = emailService;
    }

    @GetMapping
    public List<SupportDTO> getAllSupportMessages() {
        return supportMessageService.getAllSupportMessages();
    }

    @GetMapping("/{supportMessageId}")
    public SupportDTO getSupportMessage(@PathVariable("supportMessageId") Long supportMessageId) {
        return supportMessageService.getSupportMessageById(supportMessageId);
    }

    @GetMapping("/search")
    public List<SupportDTO> search(@RequestParam(required = false) String email,
                                   @RequestParam(required = false) LocalDate date) {
        return supportMessageService.search(email, date);
    }

    @PostMapping("/{supportMessageId}/reply")
    @ResponseStatus(HttpStatus.CREATED)
    public SupportDTO replyToMessage(@PathVariable Long supportMessageId, @RequestBody SupportReplyDTO replyDTO) throws ResponseStatusException {
        SupportDTO originalMsg = supportMessageService.getSupportMessageById(supportMessageId);

        String targetEmail = originalMsg.getUserEmail();
        if (targetEmail == null || targetEmail.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Email not found");
        }

        String replyText = "RE: Support Request (ID: " + supportMessageId + ")\n\n" + replyDTO.getReplyText();

        if (userService.checkEmail(targetEmail)) {
            User user = userService.findUserByEmail(targetEmail);
            notificationService.sendNotificationToClient(user, notificationService.createNotification(user, TypeNotification.SUPPORT, replyText));
            return originalMsg;
        } else {
            try {
                emailService.sendSimpleEmail(targetEmail, "Support Reply - MyBank", replyText);
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Something went wrong");
            }
        }
        return originalMsg;
    }

}
