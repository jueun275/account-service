package com.example.account.domain;

import com.example.account.type.TransactionResultType;
import com.example.account.type.TransactionType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Getter
@Entity
@NoArgsConstructor
public class Transaction extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long amount;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    private TransactionResultType transactionResultType;

    private Long balanceSnapshot;
    private String transactionId;
    private LocalDateTime transactionAt;

    @ManyToOne
    private Account account;

    @Builder
    public Transaction(Long amount,
                       TransactionType transactionType,
                       TransactionResultType transactionResultType,
                       Long balanceSnapshot,
                       String transactionId,
                       LocalDateTime transactionAt,
                       Account account) {

        this.amount = amount;
        this.transactionType = transactionType;
        this.transactionResultType = transactionResultType;
        this.balanceSnapshot = balanceSnapshot;
        this.transactionId = transactionId;
        this.transactionAt = transactionAt;
        this.account = account;
    }

}
