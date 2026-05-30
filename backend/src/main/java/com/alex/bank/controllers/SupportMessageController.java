package com.alex.bank.controllers;

import com.alex.bank.dto.support.SupportDTO;
import com.alex.bank.services.supportMessage.SupportMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/support")
@RequiredArgsConstructor
@Tag(name = "Support", description = "Public support message endpoints")
public class SupportMessageController {

    private final SupportMessageService supportMessageService;

    @Operation(summary = "Create a public support request")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SupportDTO createSupportMessage(@RequestBody SupportDTO supportDTO, java.security.Principal principal) {
        return supportMessageService.createSupportMessage(supportDTO, principal);
    }

}
