package com.example.account.controller;

import com.example.account.dto.UseBalance;
import com.example.account.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    // 잔액 사용
    @PostMapping("/transaction/use")
    public UseBalance.Response useBalance(
        @RequestBody @Valid UseBalance.Request requestDto) {
        try {
            return UseBalance.Response.from(
                transactionService.useBalance(
                    requestDto.getUserId(),
                    requestDto.getAccountNumber(),
                    requestDto.getAmount())
            );
        } catch (ArithmeticException e) {
            log.error("Failed to use balanse");
            transactionService.saveFailedUseTransaction(
                requestDto.getAccountNumber(),
                requestDto.getAmount()
            );

            throw e;
        }
    }
}
