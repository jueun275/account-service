package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.type.AccountStatus;
import com.example.account.domain.AccountUser;
import com.example.account.domain.Transaction;
import com.example.account.dto.TransactionDto;
import com.example.account.exception.AccountException;
import com.example.account.exception.ErrorCode;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.repository.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.example.account.type.TransactionResultType.FAILED;
import static com.example.account.type.TransactionResultType.SUCCESS;
import static com.example.account.type.TransactionType.CANCEL;
import static com.example.account.type.TransactionType.USE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    private static final long USE_AMOUNT = 200L;
    private static final long CANCEL_AMOUNT = 2000L;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountUserRepository accountUserRepository;

    @InjectMocks
    private TransactionService transactionService;

    @DisplayName("잔액 사용 성공 테스트")
    @Test
    void successUseBalance() {
        // given
        AccountUser accountUser = AccountUser.builder()
            .id(12L)
            .userName("testUser")
            .build();
        given(accountUserRepository.findById(anyLong()))
            .willReturn(Optional.of(accountUser));

        Account account = Account.builder()
            .accountUser(accountUser)
            .balance(10000L)
            .accountStatus(AccountStatus.IN_USE)
            .accountNumber("1000000012")
            .build();

        given(accountRepository.findByAccountNumber(anyString()))
            .willReturn(Optional.of(account));

        given(transactionRepository.save(any()))
            .willReturn(Transaction.builder()
                .account(account)
                .transactionId("transactionId")
                .transactionType(USE)
                .transactionResultType(SUCCESS)
                .amount(1000L)
                .balanceSnapshot(9000L)
                .transactionAt(LocalDateTime.now())
                .build());

        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        // when
        TransactionDto transactionDto = transactionService.useBalance(1L, "100000000", USE_AMOUNT);

        // then
        verify(transactionRepository, times(1)).save(transactionCaptor.capture());
        assertEquals(USE_AMOUNT, transactionCaptor.getValue().getAmount());
        assertEquals(9800L, transactionCaptor.getValue().getBalanceSnapshot());
        assertEquals(SUCCESS, transactionDto.getTransactionResultType());
        assertEquals(1000L, transactionDto.getAmount());
        assertEquals(9000L, transactionDto.getBalanceSnapshot());
        assertEquals(USE, transactionDto.getTransactionType());
    }

    @DisplayName("해당 유저 없음 - 잔액 사용 실패")
    @Test
    void useBalance_UserNotFound() {
        // given
        given(accountUserRepository.findById(anyLong()))
            .willReturn(Optional.empty());

        // when
        AccountException exception = assertThrows(AccountException.class,
            () -> transactionService.useBalance(1L, "1000000000", 1000L));
        // then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @DisplayName("해당 계좌 없음 - 잔액 사용 실패")
    @Test
    void useBalance_AccountNotFound() {
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
        // then
        AccountException exception = assertThrows(AccountException.class,
            () -> transactionService.useBalance(1L, "1000000000", 1000L));

        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
    }

    @DisplayName("이미 해제된 계좌는 사용할 수 없다")
    @Test
    void useBalance_AlreadyClosed() {
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
        // then
        AccountException exception = assertThrows(AccountException.class,
            () -> transactionService.useBalance(1L, "1000000000", 1000L));

        assertEquals(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED, exception.getErrorCode());
    }

    @DisplayName("계좌 소유주 다름 - 잔액 사용 실패")
    @Test
    void useBalance_UserUnMatch() {
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
        // then
        AccountException exception = assertThrows(AccountException.class,
            () -> transactionService.useBalance(1L, "1000000000", 1000L));

        assertEquals(ErrorCode.USER_ACCOUNT_UN_MATCH, exception.getErrorCode());
    }


    @DisplayName(" 거래금액이 잔액이 많아야한다")
    @Test
    void exceedAmount_UseBalance() {
        // given
        AccountUser accountUser = AccountUser.builder()
            .id(12L)
            .userName("testUser")
            .build();
        given(accountUserRepository.findById(anyLong()))
            .willReturn(Optional.of(accountUser));

        Account account = Account.builder()
            .accountUser(accountUser)
            .balance(1000L)
            .accountStatus(AccountStatus.IN_USE)
            .accountNumber("1000000012")
            .build();

        given(accountRepository.findByAccountNumber(anyString()))
            .willReturn(Optional.of(account));

        // when
        // then
        AccountException exception = assertThrows(AccountException.class,
            () -> transactionService.useBalance(1L, "1000000000", 100000L));

        assertEquals(ErrorCode.AMOUNT_EXCEED_BALANCE, exception.getErrorCode());
        verify(transactionRepository, times(0)).save(any());
    }

    @DisplayName("실패 트랜잭션 저장 성공")
    @Test
    void saveFailedUseTransaction() {
        // given
        AccountUser accountUser = AccountUser.builder()
            .id(12L)
            .userName("testUser")
            .build();
        Account account = Account.builder()
            .accountUser(accountUser)
            .balance(10000L)
            .accountStatus(AccountStatus.IN_USE)
            .accountNumber("1000000012")
            .build();

        given(accountRepository.findByAccountNumber(anyString()))
            .willReturn(Optional.of(account));

        given(transactionRepository.save(any()))
            .willReturn(Transaction.builder()
                .account(account)
                .transactionId("transactionId")
                .transactionType(USE)
                .transactionResultType(SUCCESS)
                .amount(1000L)
                .balanceSnapshot(9000L)
                .transactionAt(LocalDateTime.now())
                .build());

        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        // when
        transactionService.saveFailedUseTransaction("100000000", USE_AMOUNT);

        // then
        verify(transactionRepository, times(1)).save(transactionCaptor.capture());
        assertEquals(USE_AMOUNT, transactionCaptor.getValue().getAmount());
        assertEquals(10000L, transactionCaptor.getValue().getBalanceSnapshot());
        assertEquals(FAILED, transactionCaptor.getValue().getTransactionResultType());
    }

    @DisplayName("거래 취소 성공 테스트")
    @Test
    void successCancelBalance() {
        // given
        AccountUser accountUser = AccountUser.builder()
            .id(12L)
            .userName("testUser")
            .build();
        Account account = Account.builder()
            .accountUser(accountUser)
            .balance(10000L)
            .accountStatus(AccountStatus.IN_USE)
            .accountNumber("1000000012")
            .build();
        Transaction transaction = Transaction.builder()
            .account(account)
            .transactionId("transactionCancel")
            .transactionType(CANCEL)
            .transactionResultType(SUCCESS)
            .amount(CANCEL_AMOUNT)
            .balanceSnapshot(9000L)
            .transactionAt(LocalDateTime.now())
            .build();


        given(transactionRepository.findByTransactionId(anyString()))
            .willReturn(Optional.of(transaction));

        given(accountRepository.findByAccountNumber(anyString()))
            .willReturn(Optional.of(account));

        given(transactionRepository.save(any()))
            .willReturn(Transaction.builder()
                .account(account)
                .transactionId("transactionCancel")
                .transactionType(CANCEL)
                .transactionResultType(SUCCESS)
                .amount(CANCEL_AMOUNT)
                .balanceSnapshot(10000L)
                .transactionAt(LocalDateTime.now())
                .build());

        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        // when
        TransactionDto transactionDto = transactionService.cancelBalance("transactionCancel", "1000000012", CANCEL_AMOUNT);

        // then
        verify(transactionRepository, times(1)).save(transactionCaptor.capture());
        assertEquals(CANCEL_AMOUNT, transactionCaptor.getValue().getAmount());
        assertEquals(10000L + CANCEL_AMOUNT, transactionCaptor.getValue().getBalanceSnapshot());
        assertEquals(SUCCESS, transactionDto.getTransactionResultType());
        assertEquals(CANCEL_AMOUNT, transactionDto.getAmount());
        assertEquals(10000L, transactionDto.getBalanceSnapshot());
        assertEquals(CANCEL, transactionDto.getTransactionType());
    }

    @DisplayName("해당 거래 없음 -  잔액 사용 취소 실패")
    @Test
    void cancelBalance_TransactionNotFound() {
        // given
        given(transactionRepository.findByTransactionId(anyString()))
            .willReturn(Optional.empty());

        // when
        // then
        AccountException exception = assertThrows(AccountException.class,
            () -> transactionService.cancelBalance("transactionCancel", "1000000012", 1000L));

        assertEquals(ErrorCode.TRANSACTION_NOT_FOUND, exception.getErrorCode());
    }

    @DisplayName("해당 계좌 없음 -  잔액 사용 취소 실패")
    @Test
    void cancelBalance_AccountNotFound() {
        // given
        AccountUser accountUser = AccountUser.builder()
            .id(12L)
            .userName("testUser")
            .build();

        given(transactionRepository.findByTransactionId(anyString()))
            .willReturn(Optional.of(Transaction.builder()
                .transactionId("transactionCancel")
                .transactionType(CANCEL)
                .transactionResultType(SUCCESS)
                .amount(CANCEL_AMOUNT)
                .balanceSnapshot(10000L)
                .transactionAt(LocalDateTime.now())
                .build()));

        given(accountRepository.findByAccountNumber(anyString()))
            .willReturn(Optional.empty());

        // when
        // then
        AccountException exception = assertThrows(AccountException.class,
            () -> transactionService.cancelBalance("transactionCancel", "1000000012", 1000L));

        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
    }

    @DisplayName("거래 계좌 다름 - 잔액 사용 취소 실패")
    @Test
    void cancelBalance_AccountUnMatch() {
        // given
        Account accountUse = Account.builder()
            .id(12L)
            .balance(10000L)
            .accountStatus(AccountStatus.IN_USE)
            .accountNumber("1000000012")
            .build();

        Account accountNotUse = Account.builder()
            .id(13L)
            .balance(10000L)
            .accountStatus(AccountStatus.IN_USE)
            .accountNumber("1000000013")
            .build();

        given(transactionRepository.findByTransactionId(anyString()))
            .willReturn(Optional.of(Transaction.builder()
                .transactionId("transactionCancel")
                .account(accountUse)
                .transactionType(CANCEL)
                .transactionResultType(SUCCESS)
                .amount(CANCEL_AMOUNT)
                .balanceSnapshot(10000L)
                .transactionAt(LocalDateTime.now())
                .build()));

        given(accountRepository.findByAccountNumber(anyString()))
            .willReturn(Optional.of(accountNotUse));


        // when
        // then
        AccountException exception = assertThrows(AccountException.class,
            () -> transactionService.cancelBalance("transactionCancel", "1000000013", CANCEL_AMOUNT));

        assertEquals(ErrorCode.TRANSACTION_ACCOUNT_UN_MATCH, exception.getErrorCode());
    }

    @DisplayName("거래 금액과 취소금액이 다름- 잔액 사용 취소 실패")
    @Test
    void cancelBalance_CancelMustFully() {
        // given
        Account accountUse = Account.builder()
            .id(12L)
            .balance(10000L)
            .accountStatus(AccountStatus.IN_USE)
            .accountNumber("1000000012")
            .build();

        given(transactionRepository.findByTransactionId(anyString()))
            .willReturn(Optional.of(Transaction.builder()
                .transactionId("transactionCancel")
                .account(accountUse)
                .transactionType(CANCEL)
                .transactionResultType(SUCCESS)
                .amount(CANCEL_AMOUNT)
                .balanceSnapshot(10000L)
                .transactionAt(LocalDateTime.now())
                .build()));

        given(accountRepository.findByAccountNumber(anyString()))
            .willReturn(Optional.of(accountUse));


        // when
        // then
        AccountException exception = assertThrows(AccountException.class,
            () -> transactionService.cancelBalance("transactionCancel", "1000000013", 1000L));

        assertEquals(ErrorCode.CANCEL_MUST_FULLY, exception.getErrorCode());
    }

    @Test
    void successQueryTransaction() {
        // given
        Account accountUse = Account.builder()
            .id(12L)
            .balance(10000L)
            .accountStatus(AccountStatus.IN_USE)
            .accountNumber("1000000012")
            .build();
        Transaction transaction = Transaction.builder()
            .transactionId("transactionCancel")
            .account(accountUse)
            .transactionType(USE)
            .transactionResultType(SUCCESS)
            .amount(CANCEL_AMOUNT)
            .balanceSnapshot(10000L)
            .transactionAt(LocalDateTime.now())
            .build();


        given(transactionRepository.findByTransactionId(anyString()))
            .willReturn(Optional.of(transaction));
        // when
        TransactionDto transactionDto = transactionService.queryTransaction("trxId");
        // then
        assertEquals(USE, transactionDto.getTransactionType());
        assertEquals(SUCCESS, transactionDto.getTransactionResultType());
        assertEquals(CANCEL_AMOUNT, transactionDto.getAmount());


    }

    @DisplayName("해당 거래 없음 - 거래 조회 실패")
    @Test
    void queryTransaction_TransactionNotFound() {
        // given
        given(transactionRepository.findByTransactionId(anyString()))
            .willReturn(Optional.empty());

        // when
        // then
        AccountException exception = assertThrows(AccountException.class,
            () -> transactionService.cancelBalance("transactionId", "1000000012", 1000L));

        assertEquals(ErrorCode.TRANSACTION_NOT_FOUND, exception.getErrorCode());
    }




}