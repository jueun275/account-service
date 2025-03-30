package com.example.account.dto;

import com.example.account.domain.Account;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
public class AccountDto {
    private Long userId;
    private String accountNumber;
    private LocalDateTime createdAt;

    private LocalDateTime registeredAt;
    private LocalDateTime unRegisteredAt;

    @Builder
    public AccountDto(Long userId,
                      String accountNumber,
                      LocalDateTime createdAt,
                      LocalDateTime registeredAt,
                      LocalDateTime unRegisteredAt) {
        this.userId = userId;
        this.accountNumber = accountNumber;
        this.createdAt = createdAt;
        this.registeredAt = registeredAt;
        this.unRegisteredAt = unRegisteredAt;
    }

    public static AccountDto fromEntity(Account account) {
        return AccountDto.builder()
            .userId(account.getAccountUser().getId())
            .accountNumber(account.getAccountNumber())
            .registeredAt(account.getRegisteredAt())
            .unRegisteredAt(account.getUnregisteredAt())
            .build();
    }
}
