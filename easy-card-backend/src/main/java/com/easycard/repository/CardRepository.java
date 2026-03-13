package com.easycard.repository;

import com.easycard.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findByUserId(Long userId);
    Optional<Card> findByIdAndUserId(Long id, Long userId);
    Optional<Card> findByCardNumber(String cardNumber);
    
    @Query("SELECT c FROM Card c WHERE c.status = 'PENDING'")
    List<Card> findPendingCards();
    
    @Query("SELECT c FROM Card c WHERE c.user.id = ?1 AND c.status = 'ACTIVE'")
    List<Card> findActiveCardsByUserId(Long userId);
}
