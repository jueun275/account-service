package com.example.account.dto;

import com.example.account.domain.Account;
import com.example.account.type.AccountStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
public class AccountDto {
    private Long userId;
    private Long balance;
    private String accountNumber;
    private AccountStatus status;
    private LocalDateTime createdAt;

    private LocalDateTime registeredAt;
    private LocalDateTime unRegisteredAt;

    @Builder
    public AccountDto(Long userId,
                      Long balance,
                      String accountNumber,
                      AccountStatus status,
                      LocalDateTime createdAt,
                      LocalDateTime registeredAt,
                      LocalDateTime unRegisteredAt) {
        this.userId = userId;
        this.balance = balance;
        this.accountNumber = accountNumber;
        this.status = status;
        this.createdAt = createdAt;
        this.registeredAt = registeredAt;
        this.unRegisteredAt = unRegisteredAt;
    }

    public static AccountDto fromEntity(Account account) {
        return AccountDto.builder()
            .userId(account.getAccountUser().getId())
            .balance(account.getBalance())
            .accountNumber(account.getAccountNumber())
            .status(account.getAccountStatus())
            .registeredAt(account.getRegisteredAt())
            .unRegisteredAt(account.getUnregisteredAt())
            .build();
    }
}
