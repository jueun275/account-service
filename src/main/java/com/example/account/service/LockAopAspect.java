package com.example.account.service;

import com.example.account.aop.AccountLockIIdInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LockAopAspect {
    private final LockService lockService;

    //com.example.account.aop.AccountLock 어노테이션 달려있는 부분에서 동작하는 메서드
    @Around("@annotation(com.example.account.aop.AccountLock) && args(request)")
    public Object aroundMethod(
        ProceedingJoinPoint joinPoint,
        AccountLockIIdInterface request
    ) throws Throwable {
        // lock 취득 시도
        lockService.lock(request.getAccountNumber());

        try{
            return joinPoint.proceed();

        }finally {

            //lock 해제
            lockService.unlock(request.getAccountNumber());
        }
    }
}
