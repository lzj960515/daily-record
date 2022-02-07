package com.lzj.caffeine.deme.test;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import com.google.common.testing.FakeTicker;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

/**
 * 过期/驱逐测试
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
public class ExpireTest {

    @Test
    public void testSizeExpire() throws InterruptedException {
        Cache<Integer, String> cache = Caffeine.newBuilder()
                .maximumSize(5)
                .build();
        cache.put(1, "a");
        cache.put(2, "b");
        cache.put(3, "c");
        cache.put(4, "d");
        cache.put(5, "f");
        System.out.println(cache.asMap());
        cache.put(6, "g");
        TimeUnit.SECONDS.sleep(2);
        System.out.println(cache.getIfPresent(1));
        System.out.println(cache.asMap());
    }

    /**
     * 根据权重进行过期
     * 如果集合里面的元素超过最大权重时，就会对元素进行驱逐
     */
    @Test
    public void testWeightExpire() throws InterruptedException {
        Cache<Integer, String> cache = Caffeine.newBuilder()
                .maximumWeight(10)
                .weigher(((key, value) -> (int) key))
                .build();
        cache.put(1, "a");
        cache.put(2, "b");
        TimeUnit.SECONDS.sleep(1);
        System.out.println(cache.asMap());
        cache.put(3, "c");
        TimeUnit.SECONDS.sleep(1);
        System.out.println(cache.asMap());
        cache.put(4, "d");
        TimeUnit.SECONDS.sleep(1);
        System.out.println(cache.asMap());
        cache.put(5, "f");
        TimeUnit.SECONDS.sleep(1);
        System.out.println(cache.asMap());
        cache.put(6, "g");
        TimeUnit.SECONDS.sleep(1);
        System.out.println(cache.asMap());
    }

    /**
     * 创建/更新时间之后的指定时间过期，访问不续期
     */
    @Test
    public void testTimeWriteExpire() throws InterruptedException {
        Cache<Integer, String> cache = Caffeine.newBuilder()
                .expireAfterWrite(2, TimeUnit.SECONDS)
                // 使用ticker没有任何效果
                .ticker(new Ticker() {
                    @Override
                    public long read() {
                        long seconds = System.currentTimeMillis() / 1000 ;
//                        System.out.println(new Date((seconds + 10) * 1000));
                        return TimeUnit.SECONDS.toNanos(seconds + 10);
                    }
                })
                .build();
        cache.put(1, "a");
        cache.put(2, "b");
        cache.put(3, "c");
        cache.put(4, "d");
        cache.put(5, "f");
        cache.put(6, "g");
        System.out.println(cache.getIfPresent(1));
        TimeUnit.SECONDS.sleep(1);
        System.out.println(cache.getIfPresent(1));
        TimeUnit.SECONDS.sleep(1);
        System.out.println(cache.getIfPresent(1));
        TimeUnit.SECONDS.sleep(1);
        System.out.println(cache.getIfPresent(1));
    }

    /**
     * 读写操作一段时间之后过期，访问会续期
     */
    @Test
    public void testTimeAccessExpire() throws InterruptedException {
        Cache<Integer, String> cache = Caffeine.newBuilder()
                .expireAfterAccess(2, TimeUnit.SECONDS)
                .build();
        cache.put(1, "a");
        cache.put(2, "b");
        cache.put(3, "c");
        cache.put(4, "d");
        cache.put(5, "f");
        cache.put(6, "g");
        System.out.println(cache.getIfPresent(1));
        TimeUnit.SECONDS.sleep(1);
        System.out.println(cache.getIfPresent(1));
        TimeUnit.SECONDS.sleep(1);
        System.out.println(cache.getIfPresent(1));
        TimeUnit.SECONDS.sleep(1);
        System.out.println(cache.getIfPresent(1));
    }

    @Test
    public void testTicker(){
        FakeTicker ticker = new FakeTicker(); // Guava的测试库
        Cache<Integer, String> cache = Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .executor(Runnable::run)
                .ticker(ticker::read)
                .maximumSize(10)
                .build();
        cache.put(1, "a");
        // 让时钟过去30分钟
        ticker.advance(30, TimeUnit.MINUTES);
        MatcherAssert.assertThat(cache.getIfPresent(1), CoreMatchers.nullValue());
    }
}
