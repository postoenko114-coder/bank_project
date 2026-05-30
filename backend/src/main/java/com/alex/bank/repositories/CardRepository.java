package com.alex.bank.repositories;

import com.alex.bank.models.card.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    Boolean existsByCardNumber(String cardNumber);

    Optional<Card> findByCardNumber(String cardNumber);

    @Query("SELECT COUNT(c) > 0 FROM Card c WHERE c.id = :cardId AND c.user.id = :userId")
    boolean existsByIdAndUserId(@Param("cardId") Long cardId, @Param("userId") Long userId);
}
