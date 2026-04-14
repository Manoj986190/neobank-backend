package com.neobank.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(length = 500)
    private String description;

    @Column(name = "transaction_date", updatable = false)
    @Builder.Default
    private LocalDateTime transactionDate = LocalDateTime.now();

    // Immutable snapshot of balance AFTER this transaction
    @Column(name = "balance_after", nullable = false,
            precision = 15, scale = 2)
    private BigDecimal balanceAfter;

    public enum TransactionType {
        DEBIT, CREDIT
    }
}