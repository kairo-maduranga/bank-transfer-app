package com.example.bankapp.repository;

import com.example.bankapp.model.BankTransaction;
import com.example.bankapp.model.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface BankTransactionRepository extends JpaRepository<BankTransaction, Long> {

    @Query("SELECT b FROM BankTransaction b ORDER BY b.createdAt DESC")
    List<BankTransaction> findAllLatest();

    @Query("SELECT b FROM BankTransaction b WHERE b.fromAccount = :accountNumber OR b.toAccount = :accountNumber ORDER BY b.createdAt DESC")
    List<BankTransaction> findHistory(@Param("accountNumber") String accountNumber);

    @Query("SELECT b FROM BankTransaction b WHERE b.status = :status ORDER BY b.createdAt DESC")
    List<BankTransaction> findByStatusLatest(@Param("status") TransactionStatus status);

    @Query("SELECT COUNT(b) FROM BankTransaction b WHERE b.status = :status")
    long countByStatus(@Param("status") TransactionStatus status);

    // --- Task 12 Aluth Queries Dekam ---

    // 1. Large transfers ganna query eka[cite: 10]
    @Query("SELECT b FROM BankTransaction b WHERE b.amount > :minAmount AND b.status = 'SUCCESS' ORDER BY b.amount DESC")
    List<BankTransaction> findLargeTransfers(@Param("minAmount") BigDecimal minAmount);

    // 2. Account eken yawapu total salli ganna query eka[cite: 10]
    @Query("SELECT SUM(b.amount) FROM BankTransaction b WHERE b.fromAccount = :accountNumber AND b.status = 'SUCCESS'")
    BigDecimal getTotalSentByAccount(@Param("accountNumber") String accountNumber);
}