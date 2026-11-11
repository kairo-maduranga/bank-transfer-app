package com.example.bankapp.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @Column(name = "account_number", unique = true)
    private String accountNumber;

    @Column(name = "full_name")
    private String fullName;

    @Column(unique = true)
    private String email;

    // Salli (money) sanda BigDecimal pawichi karanna
    @Column(precision = 14, scale = 2)
    private BigDecimal balance;
}