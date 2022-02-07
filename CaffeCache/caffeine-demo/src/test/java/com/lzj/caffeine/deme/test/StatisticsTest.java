package com.lzj.caffeine.deme.test;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

/**
 * 测试统计功能
 * @author Zijian Liao
 * @since 1.0.0
 */
public class StatisticsTest {

    @Test
    public void testStatistics(){
        Cache<Integer, String> cache = Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(10_000)
                // 开启统计
                .recordStats()
                .build();
        cache.put(1, "a");
        cache.put(2, "b");
        cache.put(3, "c");
        cache.put(4, "d");
        cache.put(5, "f");
        cache.put(6, "g");
        cache.getIfPresent(1);
        cache.getIfPresent(2);
        cache.getIfPresent(7);
        // hitRate(): 查询缓存的命中率
        // evictionCount(): 被驱逐的缓存数量
        // averageLoadPenalty(): 新值被载入的平均耗时
        System.out.println(cache.stats());
    }
}
