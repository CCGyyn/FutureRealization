package com.ccg.futurerealization.aop;

import android.view.View;

import com.ccg.futurerealization.utils.LogUtils;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.SourceLocation;

/**
 * @Description:
 * @Author: cgaopeng
 * @CreateDate: 21-12-8 上午11:39
 * @Version: 1.0
 */
@Aspect
public class LogTraceAspect {

    @Pointcut("execution(@com.ccg.futurerealization.aop.LogTrace * *(..))")
    public void methodAnnotatedWithLogTrace() {
    }

    @Around("methodAnnotatedWithLogTrace()")
    public void joinPointWithLog(ProceedingJoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        long startTime = System.currentTimeMillis();
        try {
            joinPoint.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        LogUtils.d("times=" + (System.currentTimeMillis() - startTime) + ", signature=" + signature);
    }
}
