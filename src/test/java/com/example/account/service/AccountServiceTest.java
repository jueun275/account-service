package com.example.account.service;


import com.example.account.domain.Account;
import com.example.account.domain.AccountStatus;
import com.example.account.domain.AccountUser;
import com.example.account.dto.AccountDto;
import com.example.account.exception.AccountException;
import com.example.account.exception.ErrorCode;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountUserRepository accountUserRepository;

    @InjectMocks
    private AccountService accountService;

    @DisplayName("계좌생성-성공테스트")
    @Test
    void craeteAccontSeccess() {
        // given
        AccountUser accountUser = AccountUser.builder()
            .id(12L)
            .userName("testUser")
            .build();
        
        given(accountUserRepository.findById(anyLong()))
            .willReturn(Optional.of(accountUser));

        given(accountRepository.findFirstByOrderByIdDesc())
            .willReturn(Optional.of(Account.builder()
                .accountUser(accountUser)
                .accountNumber("1000000012")
                .build()));

        given(accountRepository.save(any()))
            .willReturn(Account.builder()
                .accountUser(accountUser)
                .accountNumber("1000000013")
                .build());

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        // when
        AccountDto accountDto = accountService.createAccount(1L, 1000L);
        // then
        verify(accountRepository, times(1)).save(accountCaptor.capture());
        assertEquals(12L, accountDto.getUserId());
        assertEquals("1000000013", accountCaptor.getValue().getAccountNumber());

    }

    @DisplayName("첫 계좌 생성 테스트")
    @Test
    void createFirstAccount() {
        // given
        AccountUser accountUser = AccountUser.builder()
            .id(12L)
            .userName("testUser")
            .build();

        given(accountUserRepository.findById(anyLong()))
            .willReturn(Optional.of(accountUser));

        given(accountRepository.findFirstByOrderByIdDesc())
            .willReturn(Optional.empty());

        given(accountRepository.save(any()))
            .willReturn(Account.builder()
                .accountUser(accountUser)
                .accountNumber("1000000013")
                .build());

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        // when
        AccountDto accountDto = accountService.createAccount(1L, 1000L);
        // then
        verify(accountRepository, times(1)).save(accountCaptor.capture());
        assertEquals(12L, accountDto.getUserId());
        assertEquals("1000000000", accountCaptor.getValue().getAccountNumber());

    }

    @DisplayName("해당 유저 없음 - 계좌 생성시패")
    @Test
    void createAccount_UserNotFound() {
        // given
        given(accountUserRepository.findById(anyLong()))
            .willReturn(Optional.empty());

         // when
        AccountException exception = assertThrows(AccountException.class, () -> accountService.createAccount(1L, 1000L));
        // then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @DisplayName("유저 당 최대 계좌는 10개 ")
    @Test
    void createAccount_maxAccountIs10() {
        // given
        AccountUser accountUser = AccountUser.builder()
            .id(15L)
            .userName("testUser")
            .build();

        given(accountUserRepository.findById(anyLong()))
            .willReturn(Optional.of(accountUser));

        given(accountRepository.countByAccountUser(accountUser))
            .willReturn(10);


        // when
        AccountException exception = assertThrows(AccountException.class,
            () -> accountService.createAccount(1L, 1000L));
        // then
        assertEquals(ErrorCode.USER_ACCOUNT_PER_USER_10,
            exception.getErrorCode());
    }




}