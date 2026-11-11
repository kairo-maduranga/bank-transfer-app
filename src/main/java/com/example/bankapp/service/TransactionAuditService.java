package com.example.bankapp.service;

import com.example.bankapp.model.BankTransaction;
import com.example.bankapp.model.TransactionStatus;
import com.example.bankapp.repository.BankTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransactionAuditService {

    private final BankTransactionRepository transactionRepository;

    // REQUIRES_NEW ensures this gets saved even if the main transaction is rolled back[cite: 8]
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(String fromAccount, String toAccount, BigDecimal amount, String errorMsg) {
        BankTransaction failedTx = new BankTransaction();
        failedTx.setFromAccount(fromAccount);
        failedTx.setToAccount(toAccount);
        failedTx.setAmount(amount);
        failedTx.setStatus(TransactionStatus.FAILED);
        failedTx.setDescription(errorMsg);
        transactionRepository.save(failedTx);
    }
}