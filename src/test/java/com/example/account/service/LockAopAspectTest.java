package com.example.account.service;

import com.example.account.dto.UseBalance;
import com.example.account.exception.AccountException;
import com.example.account.exception.ErrorCode;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.example.account.exception.ErrorCode.ACCOUNT_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@ExtendWith(MockitoExtension.class)
class LockAopAspectTest {

    @Mock
    private LockService lockService;

    @Mock
    private ProceedingJoinPoint proceedingJoinPoint;

    @InjectMocks
    private LockAopAspect lockAopAspect;


    @Test
    void lockAndUnlock() throws Throwable {
        // given
        ArgumentCaptor<String> lockCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> unLockCaptor = ArgumentCaptor.forClass(String.class);

        UseBalance.Request request =
            new UseBalance.Request(123L, "1234", 1000L);

        // when
        lockAopAspect.aroundMethod(proceedingJoinPoint, request);

        // then
        verify(lockService, times(1))
            .lock(lockCaptor.capture());
        verify(lockService, times(1))
            .unlock(unLockCaptor.capture());
        assertEquals("1234", lockCaptor.getValue());
        assertEquals("1234", unLockCaptor.getValue());

    }

    @Test
    void lockAndUnlock_evenIdThrow() throws Throwable {
        // given
        ArgumentCaptor<String> lockCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> unLockCaptor = ArgumentCaptor.forClass(String.class);

        UseBalance.Request request =
            new UseBalance.Request(123L, "1234", 1000L);

        given(proceedingJoinPoint.proceed())
            .willThrow(new AccountException(ACCOUNT_NOT_FOUND));

        // when
        assertThrows(AccountException.class, () ->
            lockAopAspect.aroundMethod(proceedingJoinPoint, request));

        // then
        verify(lockService, times(1))
            .lock(lockCaptor.capture());
        verify(lockService, times(1))
            .unlock(unLockCaptor.capture());
        assertEquals("1234", lockCaptor.getValue());
        assertEquals("1234", unLockCaptor.getValue());

    }
}