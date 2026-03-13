package com.easycard.repository;

import com.easycard.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByCardIdOrderByTransactionDateDesc(Long cardId);
    List<Transaction> findByCardUserIdOrderByTransactionDateDesc(Long userId);
}
