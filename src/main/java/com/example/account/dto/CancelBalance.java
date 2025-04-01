package com.example.account.dto;

import com.example.account.aop.AccountLockIIdInterface;
import com.example.account.type.TransactionResultType;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

public class CancelBalance {

    @Setter
    @Getter
    @NoArgsConstructor
    public static class Request implements AccountLockIIdInterface {
        private String accountNumber;
        private String transactionId;
        private Long amount;

        public Request(String accountNumber, String transactionId, Long amount) {
            this.accountNumber = accountNumber;
            this.transactionId = transactionId;
            this.amount = amount;
        }
    }

    @Setter
    @Getter
    @NoArgsConstructor
    public static class Response {
        private String accountNumber;
        private TransactionResultType transactionResultType;
        private String transactionId;
        private Long amount;
        private LocalDateTime transactedAt;

        @Builder
        public Response(String accountNumber,
                        TransactionResultType transactionResultType,
                        String transactionId, Long amount,
                        LocalDateTime transactedAt) {

            this.accountNumber = accountNumber;
            this.transactionResultType = transactionResultType;
            this.transactionId = transactionId;
            this.amount = amount;
            this.transactedAt = transactedAt;
        }

        public static Response from(TransactionDto transactionDto) {
            return Response.builder()
                .accountNumber(transactionDto.getAccountNumber())
                .transactionResultType(transactionDto.getTransactionResultType())
                .transactionId(transactionDto.getTransactionId())
                .amount(transactionDto.getAmount())
                .transactedAt(transactionDto.getTransactedAt())
                .build();
        }
    }
}
