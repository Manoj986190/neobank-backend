package com.neobank.backend.account;

import com.neobank.backend.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByUserId(Long userId);

    boolean existsByAccountNumber(String accountNumber);
}