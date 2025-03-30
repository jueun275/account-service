package com.example.account.controller;


import com.example.account.dto.AccountDto;
import com.example.account.dto.CreateAccount;
import com.example.account.service.AccountService;
import com.example.account.service.RedisTestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
