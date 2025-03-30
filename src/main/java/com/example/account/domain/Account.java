package com.example.account.domain;


import com.example.account.exception.AccountException;
import com.example.account.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Account extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AccountUser accountUser;

    @Column(unique = true, nullable = false)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus;

    private Long balance;
    private LocalDateTime registeredAt;
    private LocalDateTime unregisteredAt;

    public void unregisterAccount() {
        this.accountStatus = AccountStatus.UNREGISTERED;
        this.unregisteredAt = LocalDateTime.now();
    }

    @Builder
    public Account(AccountUser accountUser, String accountNumber, Long balance) {
        this.accountUser = accountUser;
        this.accountNumber = accountNumber;
        this.accountStatus = AccountStatus.IN_USE;
        this.balance = balance;
        this.registeredAt = LocalDateTime.now();
    }

    public void useBalance(Long amount) {
        if(this.balance < amount) {
            throw new AccountException(ErrorCode.AMOUNT_EXCEED_BALANCE);
        }
        balance -= amount;
    }
}
