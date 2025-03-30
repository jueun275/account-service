package com.example.account.controller;

import com.example.account.dto.AccountDto;
import com.example.account.dto.CloseAccount;
import com.example.account.dto.CreateAccount;
import com.example.account.service.AccountService;
import com.example.account.service.RedisTestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private RedisTestService redisTestService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("계좌 생성 테스트")
    @Test
    void successCreateAccount() throws Exception {
        // given
        given(accountService.createAccount(anyLong(), anyLong()))
            .willReturn(AccountDto.builder()
                .userId(1L)
                .accountNumber("12345678")
                .registeredAt(LocalDateTime.now())
                .unRegisteredAt(LocalDateTime.now())
                .build());

        // when
        // then
        mockMvc.perform(post("/account")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new CreateAccount.Request(3333L, 1111L)
            )))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(1))
            .andDo(print());
    }

    @Test
    void successCloseAccount() throws Exception {
        // given
        given(accountService.closeAccount(anyLong(), anyString()))
            .willReturn(AccountDto.builder()
                .userId(1L)
                .accountNumber("1234567890")
                .registeredAt(LocalDateTime.now())
                .unRegisteredAt(LocalDateTime.now())
                .build());

        // when
        // then
        mockMvc.perform(delete("/account")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new CloseAccount.Request(3333L, "1234567890")
                )))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accountNumber").value("1234567890"))
            .andDo(print());
    }

    @Test
    void successGetAccountById() throws Exception {
        // given
        List<AccountDto> accounts = List.of(
            AccountDto.builder()
                .accountNumber("1234567890")
                .balance(1000L)
                .build(),
            AccountDto.builder()
                .accountNumber("1000000001")
                .balance(2000L)
                .build(),
            AccountDto.builder()
                .accountNumber("1111111111")
                .balance(2000L)
                .build()
        );

        given(accountService.getAccountByUserId(anyLong()))
            .willReturn(accounts);

        // when // then
        mockMvc.perform(get("/account?user_id=1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].accountNumber").value("1234567890"))
            .andExpect(jsonPath("$[0].balance").value(1000))
            .andExpect(jsonPath("$[1].accountNumber").value("1000000001"))
            .andExpect(jsonPath("$[1].balance").value(2000))
            .andDo(print());
    }

}