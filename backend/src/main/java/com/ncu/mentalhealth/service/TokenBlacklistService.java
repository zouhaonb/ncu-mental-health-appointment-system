package com.ncu.mentalhealth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String BLACKLIST_PREFIX = "token:blacklist:";

    public void addToBlacklist(String token, long expirationSeconds) {
        String key = BLACKLIST_PREFIX + token;
        stringRedisTemplate.opsForValue().set(key, "blacklisted", expirationSeconds, TimeUnit.SECONDS);
        log.info("Token已加入黑名单，有效期: {} 秒", expirationSeconds);
    }

    public boolean isBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        Boolean exists = stringRedisTemplate.hasKey(key);
        return exists != null && exists;
    }

    public void removeFromBlacklist(String token) {
        String key = BLACKLIST_PREFIX + token;
        stringRedisTemplate.delete(key);
        log.info("Token已从黑名单移除");
    }
}
