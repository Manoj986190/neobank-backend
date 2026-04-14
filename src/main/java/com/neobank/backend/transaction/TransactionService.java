package com.neobank.backend.transaction;

import com.neobank.backend.account.AccountRepository;
import com.neobank.backend.entity.*;
import com.neobank.backend.entity.Transaction.TransactionType;
import com.neobank.backend.exception.ResourceNotFoundException;
import com.neobank.backend.transaction.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository     accountRepository;

    @Transactional
    public TransactionResponse createTransaction(Long accountId,
                                                  String email,
                                                  TransactionRequest request) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Account not found: " + accountId));

        // Ownership check
        if (!account.getUser().getEmail().equals(email)) {
            throw new AccessDeniedException(
                "You do not have access to this account");
        }

        BigDecimal amount  = request.getAmount();
        BigDecimal balance = account.getBalance();

        if (request.getType() == TransactionType.DEBIT) {
            // BR-02: overdraft prevention
            if (amount.compareTo(balance) > 0) {
                throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Insufficient balance. Available: ₹" + balance);
            }
            balance = balance.subtract(amount);
        } else {
            balance = balance.add(amount);
        }

        // Atomic balance update
        account.setBalance(balance);
        accountRepository.save(account);

        // Immutable transaction record
        Transaction transaction = Transaction.builder()
                .account(account)
                .type(request.getType())
                .amount(amount)
                .description(request.getDescription())
                .balanceAfter(balance)
                .build();

        Transaction saved = transactionRepository.save(transaction);
        log.debug("Transaction {} of ₹{} on account {}",
                request.getType(), amount, accountId);

        return TransactionResponse.from(saved);
    }

    public Page<TransactionResponse> getTransactions(Long accountId,
                                                      String email,
                                                      int page,
                                                      int size) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Account not found: " + accountId));

        // Ownership check
        if (!account.getUser().getEmail().equals(email)) {
            throw new AccessDeniedException(
                "You do not have access to this account");
        }

        // Per SRS: sorted date descending
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("transactionDate").descending());

        return transactionRepository
                .findByAccountId(accountId, pageable)
                .map(TransactionResponse::from);
    }
}