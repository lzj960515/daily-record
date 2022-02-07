package com.my.response.demo.test;

import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Zijian Liao
 * @since 1.0.0
 */
public class FutureTest {

    @Test
    public void testFuture(){
        ExecutorService executorService = Executors.newFixedThreadPool(1);

        Future<Object> future = executorService.submit(() -> {
            System.out.println("callable");
            // 模拟调用业务代码
            TimeUnit.SECONDS.sleep(2);
            return "ok";
        });
        try {
            // 获取响应，超过1秒抛出超时异常
            Object o = future.get(1, TimeUnit.SECONDS);
            System.out.println(o);
        } catch (InterruptedException e) {
            System.out.println("被中断了");
        } catch (ExecutionException e) {
            System.out.println("执行任务发生异常");
        } catch (TimeoutException e) {
            System.out.println("超时");
        }
    }
}
