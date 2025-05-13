package org.kmb.eventhub.auth.service;

import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {

    private final int MAX_ATTEMPT = 5;
    private final long LOCK_TIME_DURATION = TimeUnit.MINUTES.toMillis(5);
    private final ConcurrentHashMap<String, Integer> attemptsCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> lockTimeCache = new ConcurrentHashMap<>();

    public void loginFailed(String username) {
        int attempts = attemptsCache.getOrDefault(username, 0);
        attemptsCache.put(username, attempts + 1);
        if (attempts + 1 >= MAX_ATTEMPT) {
            lockTimeCache.put(username, System.currentTimeMillis());
        }
    }

    public boolean isBlocked(String username) {
        Long lockTime = lockTimeCache.get(username);
        int attempts = attemptsCache.getOrDefault(username, 0);
        if (Objects.nonNull(lockTime) && System.currentTimeMillis() - lockTime < LOCK_TIME_DURATION) {
            return true;
        }
        if (attempts >= MAX_ATTEMPT) {
            resetAttempts(username);
        }
        return false;
    }

    public void resetAttempts(String username) {
        attemptsCache.remove(username);
        lockTimeCache.remove(username);
    }
}
