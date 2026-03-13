package com.easycard.service;

import com.easycard.dto.request.CardApplyRequest;
import com.easycard.dto.response.CardResponse;
import com.easycard.entity.Card;
import com.easycard.entity.User;
import com.easycard.repository.CardRepository;
import com.easycard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private static final SecureRandom random = new SecureRandom();

    @Transactional
    public CardResponse applyForCard(Long userId, CardApplyRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String cardNumber = generateCardNumber();
        String cvv = generateCVV();
        LocalDate expiryDate = LocalDate.now().plusYears(5);

        Card card = Card.builder()
                .cardNumber(cardNumber)
                .cardHolderName(request.getCardHolderName())
                .expiryDate(expiryDate)
                .cvv(cvv)
                .creditLimit(request.getCreditLimit())
                .availableBalance(request.getCreditLimit())
                .status(Card.CardStatus.PENDING)
                .user(user)
                .build();

        cardRepository.save(card);
        return CardResponse.fromEntity(card);
    }

    public CardResponse getCardById(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        return CardResponse.fromEntity(card);
    }

    public List<CardResponse> getUserCards(Long userId) {
        return cardRepository.findByUserId(userId).stream()
                .map(CardResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<CardResponse> getAllCards() {
        return cardRepository.findAll().stream()
                .map(CardResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<CardResponse> getPendingCards() {
        return cardRepository.findPendingCards().stream()
                .map(CardResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public CardResponse activateCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        card.setStatus(Card.CardStatus.ACTIVE);
        cardRepository.save(card);
        return CardResponse.fromEntity(card);
    }

    @Transactional
    public CardResponse blockCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        card.setStatus(Card.CardStatus.BLOCKED);
        cardRepository.save(card);
        return CardResponse.fromEntity(card);
    }

    @Transactional
    public CardResponse updateLimit(Long cardId, BigDecimal newLimit) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        BigDecimal currentBalance = card.getAvailableBalance();
        BigDecimal currentLimit = card.getCreditLimit();
        BigDecimal difference = newLimit.subtract(currentLimit);

        card.setCreditLimit(newLimit);
        card.setAvailableBalance(currentBalance.add(difference));
        cardRepository.save(card);
        return CardResponse.fromEntity(card);
    }

    private String generateCardNumber() {
        StringBuilder sb = new StringBuilder("4");
        for (int i = 0; i < 14; i++) {
            sb.append(random.nextInt(10));
        }
        String partial = sb.toString();
        int checkDigit = calculateLuhnCheckDigit(partial);
        return partial + checkDigit;
    }

    private int calculateLuhnCheckDigit(String partial) {
        int sum = 0;
        boolean alternate = true;
        for (int i = partial.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(partial.charAt(i));
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            alternate = !alternate;
        }
        return (10 - (sum % 10)) % 10;
    }

    private String generateCVV() {
        return String.format("%03d", random.nextInt(1000));
    }

    public boolean validateCardNumber(String cardNumber) {
        return isValidLuhn(cardNumber);
    }

    private boolean isValidLuhn(String cardNumber) {
        if (cardNumber == null || !cardNumber.matches("\\d+")) {
            return false;
        }
        int sum = 0;
        boolean alternate = false;
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            alternate = !alternate;
        }
        return sum % 10 == 0;
    }
}
