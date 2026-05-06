package com.example.demo.controllers.admin;

import com.example.demo.dto.CardDTO;
import com.example.demo.security.IsOwner;
import com.example.demo.services.account.AccountService;
import com.example.demo.services.card.CardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/{userId}/cards")
@PreAuthorize("hasRole('ADMIN')")
@IsOwner
public class AdminCardController {

    private final CardService cardService;

    private final AccountService accountService;

    public AdminCardController(CardService cardService, AccountService accountService) {
        this.cardService = cardService;
        this.accountService = accountService;
    }

    @GetMapping
    public List<CardDTO> getUserCards(@PathVariable Long userId) {
        return  cardService.getListUserCards(userId);
    }

    @GetMapping ("/{cardId}")
    public CardDTO getUserCard(@PathVariable Long cardId) {
        return cardService.getCardById(cardId);
    }

    @GetMapping("/filter/number")
    public CardDTO getUserCardByNumber(@RequestParam String cardNumber) {
        return cardService.findCardByNumber(cardNumber);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CardDTO createUserCard(@RequestParam String accountNumber, @RequestParam String typeCard) {
        CardDTO newCard =  cardService.createCard(accountService.getAccountByNumber(accountNumber).getId(), typeCard);
        return newCard;
    }

    @PutMapping("/{cardId}/blockCard")
    public ResponseEntity<CardDTO> blockUserCard(@PathVariable Long cardId) {
        CardDTO cardDTO = cardService.blockCard(cardId);
        return ResponseEntity.ok(cardDTO);
    }

    @PutMapping("/{cardId}/activateCard")
    public ResponseEntity<CardDTO> activateUserCard(@PathVariable Long cardId) {
        CardDTO cardDTO = cardService.activateCard(cardId);
        return ResponseEntity.ok(cardDTO);
    }

    @PutMapping("/{cardId}/changeTypeCard")
    public ResponseEntity<CardDTO> changeTypeCard(@PathVariable Long cardId) {
        CardDTO cardDTO = cardService.changeTypeCard(cardId);
        return ResponseEntity.ok(cardDTO);
    }

    @DeleteMapping("/{cardId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUserCard(@PathVariable Long cardId) {
        cardService.deleteCard(cardId);
    }

}
