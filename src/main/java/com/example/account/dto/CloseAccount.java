package com.example.account.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

public class CloseAccount {

    @Setter
    @Getter
    public static class Request {
        @NotNull
        @Min(1)
        private Long userId;

        public Request(Long userId, String accountNumber) {
            this.userId = userId;
            this.accountNumber = accountNumber;
        }

        @NotBlank
        @Size(min = 10, max = 10)
        private String accountNumber;
    }

    @Setter
    @Getter
    @NoArgsConstructor
    public static class Response {
        private Long userId;
        private String accountNumber;
        private LocalDateTime unRegisteredAt;

        @Builder
        public Response(Long userId, String accountNumber, LocalDateTime unRegisteredAt) {
            this.userId = userId;
            this.accountNumber = accountNumber;
            this.unRegisteredAt = unRegisteredAt;
        }

        public static Response from(AccountDto accountDto) {
            return Response.builder()
                .userId(accountDto.getUserId())
                .accountNumber(accountDto.getAccountNumber())
                .unRegisteredAt(accountDto.getUnRegisteredAt())
                .build();
        }
    }
}
