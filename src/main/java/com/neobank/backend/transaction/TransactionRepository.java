package com.neobank.backend.transaction;

import com.neobank.backend.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository
        extends JpaRepository<Transaction, Long> {

    // Per SRS: paginated, sorted date descending
    Page<Transaction> findByAccountId(Long accountId, Pageable pageable);
}