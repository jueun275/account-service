package com.example.account.controller;

import com.example.account.aop.AccountLock;
import com.example.account.dto.CancelBalance;
import com.example.account.dto.QueryTransactionResponse;
import com.example.account.dto.UseBalance;
import com.example.account.exception.AccountException;
import com.example.account.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    // 잔액 사용
    @PostMapping("/transaction/use")
    @AccountLock
    public UseBalance.Response useBalance(
        @RequestBody @Valid UseBalance.Request requestDto
    ) throws InterruptedException {
        try {
//            Thread.sleep(5000L);
            return UseBalance.Response.from(
                transactionService.useBalance(
                    requestDto.getUserId(),
                    requestDto.getAccountNumber(),
                    requestDto.getAmount())
            );
        } catch (AccountException e) {

            log.error("Failed to use balance");

            transactionService.saveFailedUseTransaction(
                requestDto.getAccountNumber(),
                requestDto.getAmount()
            );

            throw e;
        }
    }

    @PostMapping("/transaction/cancel")
    @AccountLock
    public CancelBalance.Response cancelBalance(
        @RequestBody @Valid CancelBalance.Request requestDto) {
        try {
            return CancelBalance.Response.from(
                transactionService.cancelBalance(
                    requestDto.getTransactionId(),
                    requestDto.getAccountNumber(),
                    requestDto.getAmount())
            );
        } catch (ArithmeticException e) {
            log.error("Failed to cancel balance");
            transactionService.saveFailedCancelTransaction(
                requestDto.getAccountNumber(),
                requestDto.getAmount()
            );

            throw e;
        }
    }

    @GetMapping("/transaction/{transactionId}")
    public QueryTransactionResponse queryTransaction(
        @PathVariable String transactionId) {
        return QueryTransactionResponse.from(
            transactionService.queryTransaction(transactionId)
        );
    }
}
