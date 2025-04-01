package com.example.account.dto;

import com.example.account.type.TransactionResultType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

public class UseBalance {
    @Getter
    @Setter
    public static class Request {
        @NotNull
        private Long userId;

        @NotBlank
        private String accountNumber;

        @NotNull
        @Min(10)
        @Max(1000_000_000)
        private Long amount;

        public Request(Long userId, String accountNumber, Long amount) {
            this.userId = userId;
            this.accountNumber = accountNumber;
            this.amount = amount;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Response {
        private String accountNumber;
        private TransactionResultType transactionResultType;
        private String transactionId;
        private Long amount;
        private LocalDateTime transactedAt;

        @Builder
        public Response(String accountNumber, TransactionResultType transactionResultType, String transactionId, Long amount, LocalDateTime transactedAt) {
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
