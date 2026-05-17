package com.example.demo.service;

import jakarta.annotation.PostConstruct;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class BlockedUserService {

    private final RedisTemplate<String, String> redisTemplate;

    public BlockedUserService(
            RedisTemplate<String, String> redisTemplate
    ) {

        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void init() {

        redisTemplate.opsForSet()
                .add("blocked_numbers",
                        "9999999999",
                        "8888888888");
    }

    public boolean isBlocked(String phoneNumber) {

        return Boolean.TRUE.equals(
                redisTemplate.opsForSet()
                        .isMember(
                                "blocked_numbers",
                                phoneNumber
                        )
        );
    }
}