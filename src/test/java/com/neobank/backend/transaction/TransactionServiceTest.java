package com.neobank.backend.transaction;

import com.neobank.backend.account.AccountRepository;
import com.neobank.backend.auth.UserRepository;
import com.neobank.backend.entity.*;
import com.neobank.backend.entity.Transaction.TransactionType;
import com.neobank.backend.exception.ResourceNotFoundException;
import com.neobank.backend.transaction.dto.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private AccountRepository     accountRepository;
    @Mock private UserRepository        userRepository;

    @InjectMocks
    private TransactionService transactionService;

    private User    owner;
    private Account account;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .id(1L)
                .email("owner@neobank.in")
                .role(User.Role.CUSTOMER)
                .isActive(true)
                .build();

        account = Account.builder()
                .id(1L)
                .user(owner)
                .accountNumber("NB123456")
                .balance(new BigDecimal("5000.00"))
                .accountType(Account.AccountType.SAVINGS)
                .build();
    }

    @Test
    void credit_increasesBalance() {
        TransactionRequest request = new TransactionRequest();
        request.setType(TransactionType.CREDIT);
        request.setAmount(new BigDecimal("1000.00"));
        request.setDescription("Salary");

        when(accountRepository.findById(1L))
            .thenReturn(Optional.of(account));
        when(accountRepository.save(any())).thenReturn(account);
        when(transactionRepository.save(any()))
            .thenAnswer(i -> i.getArgument(0));

        TransactionResponse response = transactionService
            .createTransaction(1L, "owner@neobank.in", request);

        assertEquals(0, response.getBalanceAfter()
            .compareTo(new BigDecimal("6000.00")));
    }

    @Test
    void debit_decreasesBalance() {
        TransactionRequest request = new TransactionRequest();
        request.setType(TransactionType.DEBIT);
        request.setAmount(new BigDecimal("1000.00"));
        request.setDescription("Grocery");

        when(accountRepository.findById(1L))
            .thenReturn(Optional.of(account));
        when(accountRepository.save(any())).thenReturn(account);
        when(transactionRepository.save(any()))
            .thenAnswer(i -> i.getArgument(0));

        TransactionResponse response = transactionService
            .createTransaction(1L, "owner@neobank.in", request);

        assertEquals(0, response.getBalanceAfter()
            .compareTo(new BigDecimal("4000.00")));
    }

    @Test
    void debit_overdraft_throws422() {
        TransactionRequest request = new TransactionRequest();
        request.setType(TransactionType.DEBIT);
        request.setAmount(new BigDecimal("99999.00"));

        when(accountRepository.findById(1L))
            .thenReturn(Optional.of(account));

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> transactionService
                .createTransaction(1L, "owner@neobank.in", request));

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, ex.getStatusCode());
        verify(transactionRepository, never()).save(any());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void crossUserAccess_throws403() {
        TransactionRequest request = new TransactionRequest();
        request.setType(TransactionType.CREDIT);
        request.setAmount(new BigDecimal("100.00"));

        when(accountRepository.findById(1L))
            .thenReturn(Optional.of(account));

        assertThrows(AccessDeniedException.class,
            () -> transactionService
                .createTransaction(1L, "hacker@evil.com", request));

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void invalidAccount_throws404() {
        when(accountRepository.findById(999L))
            .thenReturn(Optional.empty());

        TransactionRequest request = new TransactionRequest();
        request.setType(TransactionType.CREDIT);
        request.setAmount(new BigDecimal("100.00"));

        assertThrows(ResourceNotFoundException.class,
            () -> transactionService
                .createTransaction(999L, "owner@neobank.in", request));
    }

    @Test
    void balanceAccuracy_multipleTransactions() {
        // Start: 5000
        // Credit 2000 → 7000
        // Debit  500  → 6500
        // Debit  1500 → 5000

        when(accountRepository.findById(1L))
            .thenReturn(Optional.of(account));
        when(accountRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(transactionRepository.save(any()))
            .thenAnswer(i -> i.getArgument(0));

        TransactionRequest credit = new TransactionRequest();
        credit.setType(TransactionType.CREDIT);
        credit.setAmount(new BigDecimal("2000.00"));

        transactionService.createTransaction(1L, "owner@neobank.in", credit);
        // balance is now 7000 on the account object

        TransactionRequest debit1 = new TransactionRequest();
        debit1.setType(TransactionType.DEBIT);
        debit1.setAmount(new BigDecimal("500.00"));

        TransactionResponse r = transactionService
            .createTransaction(1L, "owner@neobank.in", debit1);

        assertEquals(0,
            r.getBalanceAfter().compareTo(new BigDecimal("6500.00")));
    }
}