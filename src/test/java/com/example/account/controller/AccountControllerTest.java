package com.example.account.controller;

import com.example.account.dto.AccountDto;
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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

}