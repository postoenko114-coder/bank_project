package com.alex.bank.security;

import com.alex.bank.repositories.UserRepository;
import com.alex.bank.repositories.AccountRepository;
import com.alex.bank.repositories.CardRepository;
import com.alex.bank.repositories.NotificationRepository;
import com.alex.bank.repositories.ReservationRepository;
import com.alex.bank.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("userSecurity")
@RequiredArgsConstructor
public class UserSecurity {

    private final UserRepository userRepository;

    private final AccountRepository accountRepository;

    private final CardRepository cardRepository;

    private final ReservationRepository reservationRepository;

    private final TransactionRepository transactionRepository;

    private final NotificationRepository notificationRepository;

    public boolean isResourceOwner(Authentication authentication, Long userId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        String currentEmail = authentication.getName();

        return userRepository.findByEmail(currentEmail)
                .map(user -> user.getId().equals(userId))
                .orElse(false);
    }

    public boolean isAccountOwner(Long userId, Long accountId) {
        return userId != null && accountId != null && accountRepository.existsByIdAndUserId(accountId, userId);
    }

    public boolean isAccountNumberOwner(Long userId, String accountNumber) {
        return userId != null && accountNumber != null && accountRepository.existsByAccountNumberAndUserId(accountNumber, userId);
    }

    public boolean isCardOwner(Long userId, Long cardId) {
        return userId != null && cardId != null && cardRepository.existsByIdAndUserId(cardId, userId);
    }

    public boolean isReservationOwner(Long userId, Long reservationId) {
        return userId != null && reservationId != null && reservationRepository.existsByIdAndUserId(reservationId, userId);
    }

    public boolean isTransactionOwner(Long userId, Long transactionId) {
        return userId != null && transactionId != null && transactionRepository.existsByIdAndUserId(transactionId, userId);
    }

    public boolean isNotificationOwner(Long userId, Long notificationId) {
        return userId != null && notificationId != null && notificationRepository.existsByIdAndUserId(notificationId, userId);
    }
}
