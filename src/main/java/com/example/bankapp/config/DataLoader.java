package com.example.bankapp.config;

import com.example.bankapp.model.Customer;
import com.example.bankapp.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final CustomerRepository customerRepository;

    @Override
    public void run(String... args) throws Exception {
        // Database eke kalin data nathnam witarak me tika add karanawa
        if (customerRepository.count() == 0) {
            Customer c1 = new Customer("100001", "Kamal", "kamal@test.com", new BigDecimal("45000.00"));
            Customer c2 = new Customer("100002", "Nimali", "nimali@test.com", new BigDecimal("30000.00"));
            Customer c3 = new Customer("100003", "Saman", "saman@test.com", new BigDecimal("10000.00"));

            customerRepository.saveAll(Arrays.asList(c1, c2, c3));
            System.out.println("Sample customers loaded successfully!");
        }
    }
}