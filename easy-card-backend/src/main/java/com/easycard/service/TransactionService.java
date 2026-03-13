package com.easycard.service;

import com.easycard.dto.request.TransactionRequest;
import com.easycard.dto.response.TransactionResponse;
import com.easycard.entity.Card;
import com.easycard.entity.Transaction;
import com.easycard.repository.CardRepository;
import com.easycard.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;

    @Transactional
    public TransactionResponse processTransaction(TransactionRequest request) {
        Card card = cardRepository.findById(request.getCardId())
                .orElseThrow(() -> new RuntimeException("Card not found"));

        if (card.getStatus() != Card.CardStatus.ACTIVE) {
            throw new RuntimeException("Card is not active");
        }

        Transaction.TransactionType type = Transaction.TransactionType.valueOf(request.getType().toUpperCase());
        BigDecimal amount = request.getAmount();

        switch (type) {
            case DEBIT -> {
                if (card.getAvailableBalance().compareTo(amount) < 0) {
                    throw new RuntimeException("Insufficient funds");
                }
                card.setAvailableBalance(card.getAvailableBalance().subtract(amount));
            }
            case CREDIT -> card.setAvailableBalance(card.getAvailableBalance().add(amount));
            case PAYMENT -> card.setAvailableBalance(card.getAvailableBalance().add(amount));
            case FEE -> {
                if (card.getAvailableBalance().compareTo(amount) < 0) {
                    throw new RuntimeException("Insufficient funds for fee");
                }
                card.setAvailableBalance(card.getAvailableBalance().subtract(amount));
            }
        }

        cardRepository.save(card);

        Transaction transaction = Transaction.builder()
                .type(type)
                .amount(amount)
                .description(request.getDescription())
                .card(card)
                .build();

        transactionRepository.save(transaction);
        return TransactionResponse.fromEntity(transaction);
    }

    public List<TransactionResponse> getCardTransactions(Long cardId) {
        return transactionRepository.findByCardIdOrderByTransactionDateDesc(cardId).stream()
                .map(TransactionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<TransactionResponse> getUserTransactions(Long userId) {
        return transactionRepository.findByCardUserIdOrderByTransactionDateDesc(userId).stream()
                .map(TransactionResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
