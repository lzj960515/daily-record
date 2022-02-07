package com.lzj.caffeine.deme.test;

//import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author Zijian Liao
 * @since 1.0.0
 */
public class SimpleTest {

    @Test
    public void testSimple() {
        Cache<String, String> cache = Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(10_000)
                .build();
        String key = "a";
        // 查找一个缓存元素，没有查找到的时候返回null
        String value = cache.getIfPresent(key);
        // 查找缓存，如果缓存不存在则生成缓存元素,如果无法生成则返回null
        value = cache.get(key, k -> "b");
        // 添加或者更新一个缓存元素
        cache.put(key, value);
        System.out.println(cache.getIfPresent(key)); //b
        // 移除一个缓存元素
        cache.invalidate(key);
        System.out.println(cache.getIfPresent(key)); //null
    }

    /**
     * 可以再获取元素时返回一个默认的值
     * 暂时没想到有什么用处
     */
    @Test
    public void testLoadingCache() {
        LoadingCache<String, String> cache = Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build(key -> "b");
        String key = "a";
        // 查找缓存，如果缓存不存在则生成缓存元素,  如果无法生成则返回null
        String value = cache.get(key);
        System.out.println(value);
        // 批量查找缓存，如果缓存不存在则生成缓存元素
        Map<String, String> values = cache.getAll(Arrays.asList("b", "c", "d"));
        System.out.println(values);
    }

//    @Test
    /*public void testAsyncCache() throws ExecutionException, InterruptedException {
        AsyncCache<String, String> cache = Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(10_000)
                .buildAsync();
        String key = "a";
        // 查找一个缓存元素， 没有查找到的时候返回null
        CompletableFuture<String> value = cache.getIfPresent(key);
        // 查找缓存元素，如果不存在，则异步生成
        value = cache.get(key, k -> "b");
        System.out.println(value.get());
        // 添加或者更新一个缓存元素
        cache.put(key, value);
        // 移除一个缓存元素
        cache.synchronous().invalidate(key);
        System.out.println(cache.getIfPresent(key));
    }*/

    /**
     * 同loadingCache方法，只是变成了异步生成，需要使用时get一下
     */
    @Test
    public void testAsyncLoadingCache() {
        AsyncLoadingCache<String, String> cache = Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                // 你可以选择: 去异步的封装一段同步操作来生成缓存元素
                .buildAsync(key -> "b");
                // 你也可以选择: 构建一个异步缓存元素操作并返回一个future
                // .buildAsync((key, executor) -> createExpensiveGraphAsync(key, executor));
        String key = "a";

        // 查找缓存元素，如果其不存在，将会异步进行生成
        CompletableFuture<String> value = cache.get(key);
        // 批量查找缓存元素，如果其不存在，将会异步进行生成
        CompletableFuture<Map<String, String>> values = cache.getAll(Arrays.asList("b", "c", "d"));

    }
}