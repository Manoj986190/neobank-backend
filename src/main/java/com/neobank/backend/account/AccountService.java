package com.neobank.backend.account;

import com.neobank.backend.account.dto.*;
import com.neobank.backend.auth.UserRepository;
import com.neobank.backend.entity.Account;
import com.neobank.backend.entity.User;
import com.neobank.backend.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository    userRepository;

    @Transactional
    public AccountResponse createAccount(String email,
                                         AccountRequest request) {
        // Ownership always from SecurityContext — never request body
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                    new ResourceNotFoundException("User not found"));

        String accountNumber = generateUniqueAccountNumber();

        Account account = Account.builder()
                .user(user)
                .accountNumber(accountNumber)
                .accountType(request.getAccountType())
                .build();

        Account saved = accountRepository.save(account);
        log.debug("Account created: {} for user: {}", accountNumber, email);
        return AccountResponse.from(saved);
    }

    public List<AccountResponse> getAccountsByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                    new ResourceNotFoundException("User not found"));

        return accountRepository.findByUserId(user.getId())
                .stream()
                .map(AccountResponse::from)
                .collect(Collectors.toList());
    }

    public AccountResponse getAccountById(Long accountId, String email) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Account not found: " + accountId));

        // BR-06: only owner can access their account
        if (!account.getUser().getEmail().equals(email)) {
            throw new AccessDeniedException(
                "You do not have access to this account");
        }

        return AccountResponse.from(account);
    }

    // Auto-generate unique account number per SRS
    private String generateUniqueAccountNumber() {
        String number;
        do {
            number = "NB" + UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 10)
                    .toUpperCase();
        } while (accountRepository.existsByAccountNumber(number));
        return number;
    }
}