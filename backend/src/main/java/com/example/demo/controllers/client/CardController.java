package com.example.demo.controllers.client;

import com.example.demo.dto.CardDTO;
import com.example.demo.security.IsOwner;
import com.example.demo.services.account.AccountService;
import com.example.demo.services.card.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/{userId}/cards")
@IsOwner
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    private final AccountService accountService;

    @GetMapping
    public List<CardDTO> getCards(@PathVariable Long userId) {
        return cardService.getListUserCards(userId);
    }

    @GetMapping("/{cardId}")
    public CardDTO getCard(@PathVariable Long userId, @PathVariable Long cardId) {
        return cardService.getCardById(cardId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CardDTO createCard(@PathVariable Long userId, @RequestParam String accountNumber, @RequestParam String typeCard) {
        return cardService.createCard(accountService.getAccountByNumber(accountNumber).getId(), typeCard);
    }

    @PutMapping("/{cardId}/closeCard")
    public CardDTO closeCard(@PathVariable Long userId, @PathVariable Long cardId) {
        CardDTO cardDTO = cardService.closeCard(cardId);
        return cardDTO;
    }

}
