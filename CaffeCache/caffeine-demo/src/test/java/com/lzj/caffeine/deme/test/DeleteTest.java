package com.lzj.caffeine.deme.test;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * 术语:
 *
 * 驱逐 缓存元素因为策略被移除
 * 失效 缓存元素被手动移除
 * 移除 由于驱逐或者失效而最终导致的结果
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
public class DeleteTest {

    @Test
    public void testDelete(){
        final Cache<Integer, String> cache = Caffeine.newBuilder()
                .build();
        cache.put(1, "a");
        cache.put(2, "b");
        cache.put(3, "c");
        cache.put(4, "d");
        cache.put(5, "f");
        cache.put(6, "g");
        System.out.println(cache.asMap());
        cache.invalidate(1);
        System.out.println(cache.asMap());
        cache.invalidateAll(Arrays.asList(1,2,3));
        System.out.println(cache.asMap());
        cache.invalidateAll();
        System.out.println(cache.asMap());
    }

    /**
     * 被驱逐(过期)时触发evictionListener，removalListener（驱逐的最终结果也是移除）
     * 被移除时触发removalListener
     */
    @Test
    public void testDeleteListener() throws InterruptedException {
        Cache<Integer, String> cache = Caffeine.newBuilder()
//                .evictionListener((Integer key, String value, RemovalCause cause) ->
//                        System.out.printf("Key %s was evicted (%s)%n", key, cause))
//                .removalListener((Integer key, String value, RemovalCause cause) ->
//                        System.out.printf("Key %s was removed (%s)%n", key, cause))
                .expireAfterAccess(1, TimeUnit.SECONDS)
                .build();
        cache.put(1, "a");
        cache.put(2, "b");
        cache.put(3, "c");
        cache.put(4, "d");
        cache.put(5, "f");
        cache.put(6, "g");
        System.out.println(cache.asMap());
        cache.invalidate(1);
        System.out.println(cache.asMap());
        cache.invalidateAll(Arrays.asList(1,2,3));
        System.out.println(cache.asMap());
        cache.invalidateAll();
        System.out.println(cache.asMap());
        cache.put(7, "h");
        System.out.println(cache.asMap());
        TimeUnit.SECONDS.sleep(2);
        System.out.println(cache.asMap());
    }
}
