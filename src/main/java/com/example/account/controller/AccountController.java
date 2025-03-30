package com.example.account.controller;


import com.example.account.dto.AccountDto;
import com.example.account.dto.AccountInfo;
import com.example.account.dto.CloseAccount;
import com.example.account.dto.CreateAccount;
import com.example.account.service.AccountService;
import com.example.account.service.RedisTestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final RedisTestService redisTestService;

    // 계좌 생성
    @PostMapping("/account")
    public CreateAccount.Response createAccount(
        @RequestBody @Valid CreateAccount.Request requestDto) {

        return CreateAccount.Response.from(
            accountService.createAccount(
                requestDto.getUserId(),
                requestDto.getInitBalance())
        );
    }

    // 계좌 조회
    @GetMapping("/account")
    public List<AccountInfo> getAccountByUserId(
        @RequestParam("user_id") Long userId) {
        return accountService.getAccountByUserId(userId).stream()
            .map(AccountInfo::from)
            .collect(Collectors.toList());
    }

    @DeleteMapping("/account")
    public CloseAccount.Response closeAccount(
        @RequestBody @Valid CloseAccount.Request requestDto) {

        return CloseAccount.Response.from(
            accountService.closeAccount(
                requestDto.getUserId(),
                requestDto.getAccountNumber())
        );
    }



}
