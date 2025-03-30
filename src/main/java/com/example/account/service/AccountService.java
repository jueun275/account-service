package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountStatus;
import com.example.account.domain.AccountUser;
import com.example.account.dto.AccountDto;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;


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
        AccountUser user = accountUserRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

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

    private void validateCreateAccount(AccountUser user) {
        if (accountRepository.countByAccountUser(user) >= 10) {
            throw new RuntimeException("최대 계좌 개수를 초과했습니다.");
        }
    }

    private String generateAccountNumber() {
        return accountRepository.findFirstByOrderByIdDesc()
            .map(account -> (Integer.parseInt((account.getAccountNumber()))) + 1 + "")
            .orElse("1000000000");
    }
}
