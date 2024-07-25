package me.zhengjie.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.TimeUnit;

public class CacheUtils {

    public static final Cache<String, String> EXPIRE_CACHE = CacheBuilder.newBuilder()
            .maximumSize(10)
            .expireAfterWrite(2, TimeUnit.HOURS)
            .build();
}
