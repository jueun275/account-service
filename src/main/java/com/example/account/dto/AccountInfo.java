package com.example.account.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class AccountInfo {
    private String accountNumber;
    private Long balance;

    @Builder
    public AccountInfo(Long balance,
                       String accountNumber) {
        this.balance = balance;
        this.accountNumber = accountNumber;
    }

    public static AccountInfo from(AccountDto accountDto) {
        return AccountInfo.builder()
            .accountNumber(accountDto.getAccountNumber())
            .balance(accountDto.getBalance())
            .build();
    }
}
