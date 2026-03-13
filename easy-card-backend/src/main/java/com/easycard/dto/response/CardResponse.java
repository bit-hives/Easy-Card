package com.easycard.dto.response;

import com.easycard.entity.Card;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardResponse {
    private Long id;
    private String maskedCardNumber;
    private String cardHolderName;
    private LocalDate expiryDate;
    private BigDecimal creditLimit;
    private BigDecimal availableBalance;
    private Card.CardStatus status;
    private LocalDateTime createdAt;
    private Long userId;
    private String userEmail;

    public static CardResponse fromEntity(Card card) {
        return CardResponse.builder()
                .id(card.getId())
                .maskedCardNumber(maskCardNumber(card.getCardNumber()))
                .cardHolderName(card.getCardHolderName())
                .expiryDate(card.getExpiryDate())
                .creditLimit(card.getCreditLimit())
                .availableBalance(card.getAvailableBalance())
                .status(card.getStatus())
                .createdAt(card.getCreatedAt())
                .userId(card.getUser().getId())
                .userEmail(card.getUser().getEmail())
                .build();
    }

    private static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        return "****-****-****-" + cardNumber.substring(cardNumber.length() - 4);
    }
}
