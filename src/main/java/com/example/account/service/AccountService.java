package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.dto.AccountDto;
import com.example.account.exception.AccountException;
import com.example.account.exception.ErrorCode;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.type.AccountStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.account.exception.ErrorCode.USER_NOT_FOUND;


@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountUserRepository accountUserRepository;

    /**
     * 사용자가 있는지 조회
     * 계좌의 번호를 생성하고
     * 계좌를 저장하고, 그 정보를 넘긴다
     */
    @Transactional
    public AccountDto createAccount(Long userId, Long initialBalance) {
        AccountUser user = getAccountUser(userId);

        validateCreateAccount(user);

        return AccountDto.fromEntity(
            accountRepository.save(Account.builder()
                .accountNumber(generateAccountNumber())
                .accountStatus(AccountStatus.IN_USE)
                .accountUser(user)
                .balance(initialBalance)
                .registeredAt(LocalDateTime.now())
                .build()));
    }

    @Transactional
    public List<AccountDto> getAccountByUserId(Long userId) {
        AccountUser user = getAccountUser(userId);

        List<Account> accounts = accountRepository.findByAccountUserAndAccountStatus(user, AccountStatus.IN_USE);

        return accounts.stream()
            .map(AccountDto::fromEntity)
            .collect(Collectors.toList());

    }

    @Transactional
    public AccountDto closeAccount(Long userId, String accountNumber) {
        AccountUser user = getAccountUser(userId);

        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

        validateCloseAccount(account, user);

        account.unregisterAccount();

       return AccountDto.fromEntity(account);

    }

    private AccountUser getAccountUser(Long userId) {
        AccountUser user = accountUserRepository.findById(userId)
            .orElseThrow(() -> new AccountException(USER_NOT_FOUND));
        return user;
    }


    private void validateCloseAccount(Account account, AccountUser user) {
        if (!account.getAccountUser().getId().equals(user.getId())) {
            throw new AccountException(ErrorCode.USER_ACCOUNT_UN_MATCH);
        }
        if (account.getAccountStatus() == AccountStatus.UNREGISTERED) {
            throw new AccountException(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED);
        }
        if (account.getBalance() > 0) {
            throw new AccountException(ErrorCode.BALANCE_NOT_EMPTY);
        }
    }


    private void validateCreateAccount(AccountUser user) {
        if (accountRepository.countByAccountUser(user) >= 10) {
            throw new AccountException(ErrorCode.USER_ACCOUNT_PER_USER_10);
        }
    }

    private String generateAccountNumber() {
        return accountRepository.findFirstByOrderByIdDesc()
            .map(account -> (Integer.parseInt((account.getAccountNumber()))) + 1 + "")
            .orElse("1000000000");
    }



}
