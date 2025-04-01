package com.example.account.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

public class CreateAccount {
    @Setter
    @Getter
    public static class Request {
        @NotNull
        private Long userId;
        @Min(1)

        private Long initBalance;

        public Request(Long userId, Long initBalance) {
            this.userId = userId;
            this.initBalance = initBalance;
        }
    }

    @Setter
    @Getter
    @NoArgsConstructor
    public static class Response {
        private Long userId;
        private String accountNumber;
        private LocalDateTime registeredAt;

        @Builder
        public Response(Long userId,
                        String accountNumber,
                        LocalDateTime registeredAt) {
            this.userId = userId;
            this.accountNumber = accountNumber;
            this.registeredAt = registeredAt;
        }

        public static Response from(AccountDto accountDto) {
            return Response.builder()
                .userId(accountDto.getUserId())
                .accountNumber(accountDto.getAccountNumber())
                .registeredAt(accountDto.getRegisteredAt())
                .build();
        }

    }
}
