package com.example.demo.controllers;

import com.example.demo.dto.support.SupportDTO;
import com.example.demo.services.supportMessage.SupportMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/support")
@RequiredArgsConstructor
public class SupportMessageController {

    private final SupportMessageService supportMessageService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SupportDTO createSupportMessage(@RequestBody SupportDTO supportDTO, java.security.Principal principal) {
        return supportMessageService.createSupportMessage(supportDTO, principal);
    }

}
