package com.neobank.backend.account.dto;

import com.neobank.backend.entity.Account.AccountType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AccountRequest {

    @NotNull(message = "Account type is required")
    private AccountType accountType;
}