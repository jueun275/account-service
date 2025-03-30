package com.example.account.controller;

import com.example.account.domain.AccountUser;
import com.example.account.dto.TransactionDto;
import com.example.account.dto.TransactionResultType;
import com.example.account.dto.UseBalance;
import com.example.account.exception.AccountException;
import com.example.account.exception.ErrorCode;
import com.example.account.service.TransactionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.example.account.dto.TransactionResultType.SUCCESS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TransactionController.class)
class transactionControllerTest {

    @MockitoBean
    private TransactionService transactionService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    void successUseBalance() throws Exception {
        // given
        given(transactionService.useBalance(anyLong(), anyString(), anyLong()))
            .willReturn(TransactionDto.builder()
                .accountNumber("1000000000")
                .transactedAt(LocalDateTime.now())
                .amount(12345L)
                .transactionId("transactionId")
                .transactionResultType(SUCCESS)
                .build());

        // when
        mockMvc.perform(post("/transaction/use")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new UseBalance.Request(1L, "2000000000", 3000L)
                ))
            ).andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accountNumber").value("1000000000"))
            .andExpect(jsonPath("$.transactionResultType").value("SUCCESS"))
            .andExpect(jsonPath("$.transactionId").value("transactionId"))
            .andExpect(jsonPath("$.amount").value(12345))
        ;
        // then

    }

}