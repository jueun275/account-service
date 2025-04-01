package com.example.account.repository;

import com.example.account.domain.Account;
import com.example.account.type.AccountStatus;
import com.example.account.domain.AccountUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Integer countByAccountUser(AccountUser user);
    Optional<Account> findByAccountNumber(String accountNumber);
    Optional<Account> findFirstByOrderByIdDesc();

    List<Account> findByAccountUser(AccountUser accountUser);

    List<Account> findByAccountUserAndAccountStatus(AccountUser user, AccountStatus accountStatus);
}
