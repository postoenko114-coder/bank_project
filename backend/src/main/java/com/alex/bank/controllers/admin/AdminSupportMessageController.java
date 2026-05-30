package com.alex.bank.controllers.admin;

import com.alex.bank.dto.support.SupportDTO;
import com.alex.bank.dto.support.SupportReplyDTO;
import com.alex.bank.dto.user.UserDTO;
import com.alex.bank.models.notification.TypeNotification;
import com.alex.bank.services.EmailService;
import com.alex.bank.services.notification.NotificationService;
import com.alex.bank.services.supportMessage.SupportMessageService;
import com.alex.bank.services.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/supportMessages")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin Support", description = "Administrative support message endpoints")
public class AdminSupportMessageController {

    private final SupportMessageService supportMessageService;

    private final UserService userService;

    private final NotificationService notificationService;

    private final EmailService emailService;

    @Operation(summary = "Get all support messages as admin")
    @GetMapping
    public List<SupportDTO> getAllSupportMessages() {
        return supportMessageService.getAllSupportMessages();
    }

    @Operation(summary = "Get a support message by id as admin")
    @GetMapping("/{supportMessageId}")
    public SupportDTO getSupportMessage(@PathVariable("supportMessageId") Long supportMessageId) {
        return supportMessageService.getSupportMessageById(supportMessageId);
    }

    @Operation(summary = "Search support messages by email or date as admin")
    @GetMapping("/search")
    public List<SupportDTO> search(@RequestParam(required = false) String email,
                                   @RequestParam(required = false) LocalDate date) {
        return supportMessageService.search(email, date);
    }

    @Operation(summary = "Reply to a support message as admin")
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
            UserDTO user = userService.findUserByEmail(targetEmail);
            notificationService.createAndSendNotification(user.getId(), TypeNotification.SUPPORT, replyText);
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
