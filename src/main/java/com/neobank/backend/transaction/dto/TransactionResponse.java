package com.neobank.backend.transaction.dto;

import com.neobank.backend.entity.Transaction;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionResponse {

    private Long            id;
    private String          type;
    private BigDecimal      amount;
    private String          description;
    private LocalDateTime   transactionDate;
    private BigDecimal      balanceAfter;

    public static TransactionResponse from(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .type(t.getType().name())
                .amount(t.getAmount())
                .description(t.getDescription())
                .transactionDate(t.getTransactionDate())
                .balanceAfter(t.getBalanceAfter())
                .build();
    }
}