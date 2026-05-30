package com.alex.bank.controllers.client;

import com.alex.bank.dto.CardDTO;
import com.alex.bank.security.IsOwner;
import com.alex.bank.services.account.AccountService;
import com.alex.bank.services.card.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/{userId}/cards")
@IsOwner
@RequiredArgsConstructor
@Tag(name = "Client Cards", description = "Client card management endpoints")
public class CardController {

    private final CardService cardService;

    private final AccountService accountService;

    @Operation(summary = "Get all cards of the authenticated user")
    @GetMapping
    public List<CardDTO> getCards(@PathVariable Long userId) {
        return cardService.getListUserCards(userId);
    }

    @Operation(summary = "Get a card of the authenticated user by id")
    @GetMapping("/{cardId}")
    @PreAuthorize("@userSecurity.isCardOwner(#userId, #cardId) or hasRole('ADMIN')")
    public CardDTO getCard(@PathVariable Long userId, @PathVariable Long cardId) {
        return cardService.getCardById(cardId);
    }

    @Operation(summary = "Create a card for a user account")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@userSecurity.isAccountNumberOwner(#userId, #accountNumber) or hasRole('ADMIN')")
    public CardDTO createCard(@PathVariable Long userId, @RequestParam String accountNumber, @RequestParam String typeCard) {
        return cardService.createCard(accountService.getAccountByNumber(accountNumber).getId(), typeCard);
    }

    @Operation(summary = "Close a user card")
    @PutMapping("/{cardId}/closeCard")
    @PreAuthorize("@userSecurity.isCardOwner(#userId, #cardId) or hasRole('ADMIN')")
    public CardDTO closeCard(@PathVariable Long userId, @PathVariable Long cardId) {
        return cardService.closeCard(cardId);
    }

}
