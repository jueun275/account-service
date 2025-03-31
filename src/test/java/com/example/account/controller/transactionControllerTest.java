package com.example.account.controller;

import com.example.account.dto.CancelBalance;
import com.example.account.dto.TransactionDto;
import com.example.account.dto.UseBalance;
import com.example.account.service.TransactionService;
import com.example.account.type.TransactionType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static com.example.account.dto.TransactionResultType.SUCCESS;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TransactionController.class)
class TransactionControllerTest {

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
                .transactionType(TransactionType.USE)
                .build());

        // when
        // then
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
    }

    @Test
    void successCancelBalance() throws Exception {
        // given
        given(transactionService.cancelBalance(anyString(), anyString(), anyLong()))
            .willReturn(TransactionDto.builder()
                .accountNumber("1000000000")
                .transactionResultType(SUCCESS)
                .transactionType(TransactionType.CANCEL)
                .transactedAt(LocalDateTime.now())
                .amount(12345L)
                .transactionId("transactionCancel")
                .build());

        // when
        // then
        mockMvc.perform(post("/transaction/cancel")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new CancelBalance.Request("2000000000","transactionId", 3000L)
                ))
            ).andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accountNumber").value("1000000000"))
            .andExpect(jsonPath("$.transactionResultType").value("SUCCESS"))
            .andExpect(jsonPath("$.transactionId").value("transactionCancel"))
            .andExpect(jsonPath("$.amount").value(12345))
        ;
    }

}