package com.example.account.dto;

import com.example.account.domain.Transaction;
import com.example.account.type.TransactionResultType;
import com.example.account.type.TransactionType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
public class TransactionDto {
    private String accountNumber;
    private TransactionResultType transactionResultType;
    private TransactionType transactionType;
    private String transactionId;
    private Long amount;
    private Long balanceSnapshot;
    private LocalDateTime transactedAt;

    @Builder
    public TransactionDto(String accountNumber,
                          String transactionId,
                          Long amount,
                          Long balanceSnapshot,
                          LocalDateTime transactedAt,
                          TransactionResultType transactionResultType,
                          TransactionType transactionType) {
        this.accountNumber = accountNumber;
        this.transactionResultType = transactionResultType;
        this.transactionType = transactionType;
        this.transactionId = transactionId;
        this.amount = amount;
        this.balanceSnapshot = balanceSnapshot;
        this.transactedAt = transactedAt;
    }

    public static TransactionDto fromEntity(Transaction transaction) {
        return TransactionDto.builder()
            .accountNumber(transaction.getAccount().getAccountNumber())
            .balanceSnapshot(transaction.getBalanceSnapshot())
            .transactionResultType(transaction.getTransactionResultType())
            .transactionType(transaction.getTransactionType())
            .transactionId(transaction.getTransactionId())
            .amount(transaction.getAmount())
            .transactedAt(transaction.getTransactionAt())
            .transactionId(transaction.getTransactionId())
            .build();
    }
}

