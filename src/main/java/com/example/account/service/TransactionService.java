package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountStatus;
import com.example.account.domain.AccountUser;
import com.example.account.domain.Transaction;
import com.example.account.dto.TransactionDto;
import com.example.account.dto.TransactionResultType;
import com.example.account.exception.AccountException;
import com.example.account.exception.ErrorCode;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.repository.TransactionRepository;
import com.example.account.type.TransactionType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import static com.example.account.dto.TransactionResultType.FAILED;
import static com.example.account.dto.TransactionResultType.SUCCESS;

@RequiredArgsConstructor
@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountUserRepository accountUserRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public TransactionDto useBalance(Long userId, String accountNumber, Long amount) {
        AccountUser accountUser = accountUserRepository.findById(userId)
            .orElseThrow(() -> new AccountException(ErrorCode.USER_NOT_FOUND));
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

        validateUseBalance(account, accountUser, amount);

        account.useBalance(amount);

        return TransactionDto.fromEntity(saveAndTransaction(amount, account, SUCCESS, TransactionType.USE));
    }

    @Transactional
    public void saveFailedUseTransaction(String accountNumber, Long amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

        saveAndTransaction(amount, account, FAILED, TransactionType.USE);
    }

    @Transactional
    public TransactionDto cancelBalance(String transactionId, String accountNumber, Long amount) {
        // 거래 조회
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
            .orElseThrow(() -> new AccountException(ErrorCode.TRANSACTION_NOT_FOUND));

        // 계좌 조회
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

        validateCancelBalance(account, transaction, amount);

        account.cancelBalance(amount);

        return TransactionDto.fromEntity(
            saveAndTransaction(amount, account, SUCCESS, TransactionType.CANCEL));
    }

    @Transactional
    public void saveFailedCancelTransaction(String accountNumber, Long amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

        saveAndTransaction(amount, account, FAILED, TransactionType.CANCEL);
    }

    @Transactional()
    public TransactionDto getTransaction(String transactionId) {
        return transactionRepository.findByTransactionId(transactionId)
            .map(TransactionDto::fromEntity)
            .orElseThrow(() -> new AccountException(ErrorCode.TRANSACTION_NOT_FOUND));
    }


    private Transaction saveAndTransaction(Long amount, Account account, TransactionResultType transactionResultType, TransactionType transactionType) {
        return transactionRepository.save(Transaction.builder()
            .transactionType(transactionType)
            .transactionResultType(transactionResultType)
            .account(account)
            .amount(amount)
            .balanceSnapshot(account.getBalance())
            .transactionId(getTransactionId())
            .transactionAt(LocalDateTime.now())
            .build());
    }

    private static String getTransactionId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private void validateUseBalance(Account account, AccountUser user, Long amount) {
        if (!Objects.equals(user.getId(), account.getAccountUser().getId())) {
            throw new AccountException(ErrorCode.USER_ACCOUNT_UN_MATCH);
        }
        if (account.getAccountStatus() != AccountStatus.IN_USE) {
            throw new AccountException(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED);
        }
        if (account.getBalance() < amount) {
            throw new AccountException(ErrorCode.AMOUNT_EXCEED_BALANCE);
        }
    }

    private void validateCancelBalance(Account account, Transaction transaction, Long amount) {
        if (!Objects.equals(transaction.getAccount().getId(), account.getId())) {
            throw new AccountException(ErrorCode.TRANSACTION_ACCOUNT_UN_MATCH);
        }
        if (!Objects.equals(transaction.getAmount(), amount)) {
            throw new AccountException(ErrorCode.CANCEL_MUST_FULLY);
        }
        if (transaction.getTransactionAt().isBefore(LocalDateTime.now().minusYears(1))) {
            throw new AccountException(ErrorCode.TOO_OLD_ORDER_TO_CANCEL);
        }
    }

}
