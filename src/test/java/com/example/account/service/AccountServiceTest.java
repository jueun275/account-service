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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
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
    void createAccountSuccess() {
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

    @DisplayName("계좌해제-성공테스트")
    @Test
    void closeAccountSuccess() {
        // given
        AccountUser accountUser = AccountUser.builder()
            .id(12L)
            .userName("testUser")
            .build();

        given(accountUserRepository.findById(anyLong()))
            .willReturn(Optional.of(accountUser));

        Account account = Account.builder()
            .accountUser(accountUser)
            .balance(0L)
            .accountNumber("1000000012")
            .accountStatus(AccountStatus.IN_USE)
            .build();

        given(accountRepository.findByAccountNumber(anyString()))
            .willReturn(Optional.of(account));

        // when
        AccountDto accountDto = accountService.closeAccount(1L, "1000000012");
        // then
        assertEquals(12L, accountDto.getUserId());
        assertEquals(AccountStatus.UNREGISTERED, accountDto.getStatus());

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

    @DisplayName("해당 유저 없음 - 계좌 해제실패")
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

    @DisplayName("해당 유저 없음 - 계좌 해제실패")
    @Test
    void closeAccount_UserNotFound() {
        // given
        given(accountUserRepository.findById(anyLong()))
            .willReturn(Optional.empty());

        // when
        AccountException exception = assertThrows(AccountException.class,
            () -> accountService.closeAccount(1L, "1234567890"));
        // then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @DisplayName("해당 계좌 없음 - 계좌 해제실패")
    @Test
    void closeAccount_AccountNotFound() {
        // given
        AccountUser accountUser = AccountUser.builder()
            .id(12L)
            .userName("testUser")
            .build();

        given(accountUserRepository.findById(anyLong()))
            .willReturn(Optional.of(accountUser));
        given(accountRepository.findByAccountNumber(anyString()))
            .willReturn(Optional.empty());

        // when
        AccountException exception = assertThrows(AccountException.class,
            () -> accountService.closeAccount(1L, "1234567890"));
        // then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
    }

    @DisplayName("계좌 소유주 다름 - 계좌 해제실패")
    @Test
    void closeAccount_UserUnMatch() {
        // given
        AccountUser harry = AccountUser.builder()
            .id(12L)
            .userName("testUser")
            .build();

        AccountUser pobi = AccountUser.builder()
            .id(13L)
            .userName("testUser")
            .build();

        given(accountUserRepository.findById(anyLong()))
            .willReturn(Optional.of(harry));

        given(accountRepository.findByAccountNumber(anyString()))
            .willReturn(Optional.of(Account.builder()
                .accountUser(pobi)
                .accountNumber("1000000012")
                .balance(0L)
                .build()));

        // when
        AccountException exception = assertThrows(AccountException.class,
            () -> accountService.closeAccount(1L, "1234567890"));
        // then
        assertEquals(ErrorCode.USER_ACCOUNT_UN_MATCH, exception.getErrorCode());
    }


    @DisplayName("해지 계좌는 잔액이 없어야 한다")
    @Test
    void closeAccount_BalanceNotEmpty() {
        // given
        AccountUser user = AccountUser.builder()
            .id(12L)
            .userName("testUser")
            .build();

        given(accountUserRepository.findById(anyLong()))
            .willReturn(Optional.of(user));

        given(accountRepository.findByAccountNumber(anyString()))
            .willReturn(Optional.of(Account.builder()
                .accountUser(user)
                .accountNumber("1000000012")
                .balance(100L)
                .build()));

        // when
        AccountException exception = assertThrows(AccountException.class,
            () -> accountService.closeAccount(1L, "1234567890"));
        // then
        assertEquals(ErrorCode.BALANCE_NOT_EMPTY, exception.getErrorCode());
    }

    @DisplayName("이미 해제된 계좌는 해지할 수 없다")
    @Test
    void closeAccount_AlreadyClosed() {
        // given
        AccountUser user = AccountUser.builder()
            .id(12L)
            .userName("testUser")
            .build();

        given(accountUserRepository.findById(anyLong()))
            .willReturn(Optional.of(user));

        given(accountRepository.findByAccountNumber(anyString()))
            .willReturn(Optional.of(Account.builder()
                .accountUser(user)
                .accountNumber("1000000012")
                .balance(0L)
                .accountStatus(AccountStatus.UNREGISTERED)
                .build()));

        // when
        AccountException exception = assertThrows(AccountException.class,
            () -> accountService.closeAccount(1L, "1234567890"));
        // then
        assertEquals(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED, exception.getErrorCode());
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

        given(accountRepository.countByAccountUser(any()))
            .willReturn(10);


        // when
        AccountException exception = assertThrows(AccountException.class,
            () -> accountService.createAccount(1L, 1000L));
        // then
        assertEquals(ErrorCode.USER_ACCOUNT_PER_USER_10,
            exception.getErrorCode());
    }

    @DisplayName("사용자 계좌 목록 조회 성공" )
    @Test
    void successGetAccountByUserId() {
        // given
        AccountUser accountUser = AccountUser.builder()
            .id(15L)
            .userName("testUser")
            .build();

        List<Account> accounts = List.of(
            Account.builder()
                .accountUser(accountUser)
                .accountStatus(AccountStatus.IN_USE)
                .accountNumber("1234567890")
                .balance(1000L)
                .build(),
            Account.builder()
                .accountUser(accountUser)
                .accountStatus(AccountStatus.IN_USE)
                .accountNumber("1000000001")
                .balance(2000L)
                .build(),
            Account.builder()
                .accountUser(accountUser)
                .accountStatus(AccountStatus.IN_USE)
                .accountNumber("1111111111")
                .balance(3000L)
                .build()
        );

        given(accountUserRepository.findById(anyLong()))
            .willReturn(Optional.of(accountUser));

        given(accountRepository.findByAccountUserAndAccountStatus(any(), eq(AccountStatus.IN_USE)))
            .willReturn(accounts);

        // when
        List<AccountDto> accountDtos = accountService.getAccountByUserId(1L);

        // then
        assertEquals(3, accountDtos.size());
        assertEquals("1234567890", accountDtos.get(0).getAccountNumber());
        assertEquals("1000000001", accountDtos.get(1).getAccountNumber());
        assertEquals("1111111111", accountDtos.get(2).getAccountNumber());
        assertEquals(1000L, accountDtos.get(0).getBalance());
        assertEquals(2000L, accountDtos.get(1).getBalance());
        assertEquals(3000L, accountDtos.get(2).getBalance());

    }


    @DisplayName("해당 유저 없음 - 계좌 목록 조회 실패")
    @Test
    void failGetAccountByUserId() {
        // given
        given(accountUserRepository.findById(anyLong()))
            .willReturn(Optional.empty());

        // when
        AccountException exception = assertThrows(AccountException.class,
            ()-> accountService.getAccountByUserId(1L));
        // then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());

    }
}