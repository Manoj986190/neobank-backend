package com.neobank.backend.account.dto;

import com.neobank.backend.entity.Account;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AccountResponse {

    private Long          id;
    private String        accountNumber;
    private BigDecimal    balance;
    private String        accountType;
    private LocalDateTime createdAt;

    public static AccountResponse from(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .accountType(account.getAccountType().name())
                .createdAt(account.getCreatedAt())
                .build();
    }
}