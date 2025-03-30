package com.example.account.repository;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    long countByAccountUser(AccountUser user);
    Optional<Account> findByAccountNumber(String accountNumber);
    Optional<Account> findFirstByOrderByIdDesc();
}
