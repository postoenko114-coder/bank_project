package com.alex.bank.controllers.admin;

import com.alex.bank.dto.CardDTO;
import com.alex.bank.services.account.AccountService;
import com.alex.bank.services.card.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/{userId}/cards")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin Cards", description = "Administrative card management endpoints")
public class AdminCardController {

    private final CardService cardService;

    private final AccountService accountService;

    @Operation(summary = "Get all cards of a user as admin")
    @GetMapping
    public List<CardDTO> getUserCards(@PathVariable Long userId) {
        return  cardService.getListUserCards(userId);
    }

    @Operation(summary = "Get a card by id as admin")
    @GetMapping ("/{cardId}")
    public CardDTO getUserCard(@PathVariable Long cardId) {
        return cardService.getCardById(cardId);
    }

    @Operation(summary = "Find a card by card number as admin")
    @GetMapping("/filter/number")
    public CardDTO getUserCardByNumber(@RequestParam String cardNumber) {
        return cardService.findCardByNumber(cardNumber);
    }

    @Operation(summary = "Create a card for a user account as admin")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CardDTO createUserCard(@RequestParam String accountNumber, @RequestParam String typeCard) {
        CardDTO newCard =  cardService.createCard(accountService.getAccountByNumber(accountNumber).getId(), typeCard);
        return newCard;
    }

    @Operation(summary = "Block a card as admin")
    @PutMapping("/{cardId}/blockCard")
    public ResponseEntity<CardDTO> blockUserCard(@PathVariable Long cardId) {
        CardDTO cardDTO = cardService.blockCard(cardId);
        return ResponseEntity.ok(cardDTO);
    }

    @Operation(summary = "Activate a card as admin")
    @PutMapping("/{cardId}/activateCard")
    public ResponseEntity<CardDTO> activateUserCard(@PathVariable Long cardId) {
        CardDTO cardDTO = cardService.activateCard(cardId);
        return ResponseEntity.ok(cardDTO);
    }

    @Operation(summary = "Change a card type as admin")
    @PutMapping("/{cardId}/changeTypeCard")
    public ResponseEntity<CardDTO> changeTypeCard(@PathVariable Long cardId) {
        CardDTO cardDTO = cardService.changeTypeCard(cardId);
        return ResponseEntity.ok(cardDTO);
    }

    @Operation(summary = "Delete a card as admin")
    @DeleteMapping("/{cardId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUserCard(@PathVariable Long cardId) {
        cardService.deleteCard(cardId);
    }

}
