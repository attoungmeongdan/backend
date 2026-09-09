package com.atmd.backend.global.auth.signupToken;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SignupTokenService {

    private static final String PREFIX = "ST:";

    private final RedisTemplate<String, String> redisTemplate;
    private final SignupTokenProperties signupTokenProperties;

    public void save(String provider, String providerId, String signupToken) {
        redisTemplate.opsForValue().set(
                key(provider, providerId),
                signupToken,
                signupTokenProperties.getExpiration(),
                TimeUnit.MILLISECONDS
        );
    }

    public boolean validate(String provider, String providerId, String signupToken) {
        String saved = redisTemplate.opsForValue().get(key(provider, providerId));
        return saved != null && saved.equals(signupToken);
    }

    public void delete(String provider, String providerId) {
        redisTemplate.delete(key(provider, providerId));
    }

    private String key(String provider, String providerId) {
        return PREFIX + provider + ":" + providerId;
    }
}
