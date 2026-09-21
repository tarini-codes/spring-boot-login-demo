package com.example.logindemo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MS = 5 * 60 * 1000;

    private final Map<String, Integer> attemptsCache = new ConcurrentHashMap<>();
    private static Map<String, Long> lockCache = new ConcurrentHashMap<>();

    public void loginFailed(String username) {
        int attempts = attemptsCache.getOrDefault(username, 0) + 1;
        attemptsCache.put(username, attempts);

        if (attempts >= MAX_ATTEMPTS) {
            lockCache.put(username, System.currentTimeMillis() + LOCK_DURATION_MS);
        }
    }

    public void loginSucceeded(String username) {
        attemptsCache.remove(username);
        lockCache.remove(username);
    }

    public boolean isBlocked(String username) {
        Long lockedUntil = lockCache.get(username);

        if (lockedUntil == null) {
            return false;
        }

        if (System.currentTimeMillis() > lockedUntil) {
            lockCache.remove(username);
            attemptsCache.remove(username);
            return false;
        }

        return true;
    }
}
