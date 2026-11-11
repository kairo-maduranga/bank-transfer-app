package com.example.bankapp.service;

import com.example.bankapp.exception.AccountNotFoundException;
import com.example.bankapp.exception.InsufficientBalanceException;
import com.example.bankapp.exception.InvalidTransferException;
import com.example.bankapp.model.BankTransaction;
import com.example.bankapp.model.Customer;
import com.example.bankapp.model.TransactionStatus;
import com.example.bankapp.repository.BankTransactionRepository;
import com.example.bankapp.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BankService {

    private final CustomerRepository customerRepository;
    private final BankTransactionRepository bankTransactionRepository;
    private static final String BANK_FEE_ACCOUNT = "999999";
    
    // Kalin thibba widiyatama rollback wena annotation eka damma[cite: 10]
    @Transactional(rollbackFor = Exception.class)
    public BankTransaction transfer(String fromAccount, String toAccount, BigDecimal amount, boolean simulateFailure) {
        
        if (fromAccount.equals(toAccount)) {
            throw new InvalidTransferException("Sender and receiver must be different accounts");
        }

        customerRepository.getByAccountNumber(fromAccount)
                .orElseThrow(() -> new AccountNotFoundException("Sender account not found: " + fromAccount));
        customerRepository.getByAccountNumber(toAccount)
                .orElseThrow(() -> new AccountNotFoundException("Receiver account not found: " + toAccount));

        // Task 12: 1% Fee Calculation[cite: 10]
        BigDecimal fee = amount.multiply(new BigDecimal("0.01")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalDeduction = amount.add(fee); 

        // 1. Sender gen okkoma salli (amount + fee) adu karanna
        int updatedRows = customerRepository.debit(fromAccount, totalDeduction);
        if (updatedRows == 0) {
            throw new InsufficientBalanceException("Insufficient balance in account " + fromAccount);
        }

        if (simulateFailure) {
            throw new RuntimeException("Simulated system failure after the withdrawal");
        }

        // 2. Receiver ta salli (amount witharak) danna
        customerRepository.credit(toAccount, amount);

        // 3. Bank eke account (999999) ekata fee eka danna[cite: 10]
        customerRepository.credit(BANK_FEE_ACCOUNT, fee);

        // 4. Transaction record eka save karanna
        BankTransaction tx = new BankTransaction();
        tx.setFromAccount(fromAccount);
        tx.setToAccount(toAccount);
        tx.setAmount(amount);
        tx.setStatus(TransactionStatus.SUCCESS);
        tx.setDescription("Transfer completed (Fee: " + fee + ")");
        
        return bankTransactionRepository.save(tx);
    }

    // Task 12: Deposit Money[cite: 10]
    @Transactional(rollbackFor = Exception.class)
    public void deposit(String accountNumber, BigDecimal amount) {
        customerRepository.credit(accountNumber, amount);
        
        BankTransaction tx = new BankTransaction();
        tx.setToAccount(accountNumber);
        tx.setAmount(amount);
        tx.setStatus(TransactionStatus.SUCCESS);
        tx.setDescription("Cash Deposit");
        bankTransactionRepository.save(tx);
    }

    // Task 12: Withdraw Money[cite: 10]
    @Transactional(rollbackFor = Exception.class)
    public void withdraw(String accountNumber, BigDecimal amount) {
        int updatedRows = customerRepository.debit(accountNumber, amount);
        if (updatedRows == 0) {
            throw new InsufficientBalanceException("Insufficient balance for withdrawal");
        }

        BankTransaction tx = new BankTransaction();
        tx.setFromAccount(accountNumber);
        tx.setAmount(amount);
        tx.setStatus(TransactionStatus.SUCCESS);
        tx.setDescription("Cash Withdrawal");
        bankTransactionRepository.save(tx);
    }

    @Transactional
    public Customer createCustomer(Customer customer) {
        if (customerRepository.countAccounts(customer.getAccountNumber()) > 0) {
            throw new RuntimeException("Account already exists");
        }
        return customerRepository.save(customer);
    }
}