package com.example.account.dto;

import com.example.account.type.TransactionResultType;
import com.example.account.type.TransactionType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class QueryTransactionResponse {
    private String accountNumber;
    private TransactionType transactionType;
    private TransactionResultType transactionResultType;
    private String transactionId;
    private Long amount;
    private LocalDateTime transactedAt;

    @Builder
    public QueryTransactionResponse(String accountNumber,
                                    TransactionType transactionType,
                                    TransactionResultType transactionResultType,
                                    String transactionId,
                                    Long amount,
                                    LocalDateTime transactedAt) {
        this.accountNumber = accountNumber;
        this.transactionType = transactionType;
        this.transactionResultType = transactionResultType;
        this.transactionId = transactionId;
        this.amount = amount;
        this.transactedAt = transactedAt;
    }

    public static QueryTransactionResponse from(TransactionDto transactionDto) {
        return QueryTransactionResponse.builder()
            .accountNumber(transactionDto.getAccountNumber())
            .transactionType(transactionDto.getTransactionType())
            .transactionResultType(transactionDto.getTransactionResultType())
            .transactionId(transactionDto.getTransactionId())
            .amount(transactionDto.getAmount())
            .transactedAt(transactionDto.getTransactedAt())
            .build();
    }


}
