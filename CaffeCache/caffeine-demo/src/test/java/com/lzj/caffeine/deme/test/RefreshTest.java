package com.lzj.caffeine.deme.test;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

/**
 * @author Zijian Liao
 * @since 1.0.0
 */
public class RefreshTest {

    /**
     * refreshAfterWrite指的是写入操作后刷新key的过期时间
     * 没琢磨出他的意义在哪里
     */
    @Test
    public void testRefresh() throws InterruptedException {
        LoadingCache<Integer, String> cache = Caffeine.newBuilder()
                .maximumSize(10_000)
                .refreshAfterWrite(1, TimeUnit.SECONDS)
                .expireAfterWrite(3, TimeUnit.SECONDS)
                .build(key -> "a");
        cache.put(1, "b");
        // put操作3秒后过期
        TimeUnit.SECONDS.sleep(2);
        // 还有1秒过期的时候get一下， 此时虽然已经过了2秒，但是由于get之后才会刷新，此时还是旧值b
        System.out.println(cache.getIfPresent(1));
        // 再get一下，此时值为a
        System.out.println(cache.getIfPresent(1));
        // 再等2s
        TimeUnit.SECONDS.sleep(2);
        // 此时已经经过了4秒，按理说key已经过期了，但是由于在上一步get时刷新了一下，key还在
        System.out.println(cache.getIfPresent(1));
        TimeUnit.SECONDS.sleep(3);
        // 上一步又get了一下，过期时间又刷新了，等待3s，此时key真的没了
        System.out.println(cache.getIfPresent(1));

    }
}
