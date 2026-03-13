package com.easycard.controller;

import com.easycard.dto.request.TransactionRequest;
import com.easycard.dto.response.TransactionResponse;
import com.easycard.entity.User;
import com.easycard.repository.UserRepository;
import com.easycard.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<TransactionResponse> processTransaction(
            @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(transactionService.processTransaction(request));
    }

    @GetMapping("/card/{cardId}")
    public ResponseEntity<List<TransactionResponse>> getCardTransactions(@PathVariable Long cardId) {
        return ResponseEntity.ok(transactionService.getCardTransactions(cardId));
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getMyTransactions(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(transactionService.getUserTransactions(user.getId()));
    }
}
