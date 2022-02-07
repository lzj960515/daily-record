package com.my.response.demo.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Zijian Liao
 * @since 1.0.0
 */
@Component
@Aspect
public class ResponseTimeAspect {

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Around("@annotation(getMapping)")
    public Object around(ProceedingJoinPoint joinPoint, GetMapping getMapping) {
        final Future<Object> future = threadPoolTaskExecutor.submit(() -> {
            try {
                return joinPoint.proceed();
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        });
        try {
            HttpServletRequest request =
                    ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
            final String uri = request.getRequestURI();
            if("/foo/a".endsWith(uri)){
                // 获取响应，超过1秒抛出超时异常
                return future.get(1, TimeUnit.SECONDS);
            }else if ("/foo/b".endsWith(uri)){
                return future.get(3, TimeUnit.SECONDS);
            }
            return future.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return "被中断了";
        } catch (ExecutionException e) {
            return "执行任务发生异常";
        } catch (TimeoutException e) {
            return "超时";
        }
    }

}
