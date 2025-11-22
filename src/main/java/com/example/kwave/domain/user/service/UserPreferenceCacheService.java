package com.example.kwave.domain.user.service;

import com.example.kwave.domain.user.domain.User;
import com.example.kwave.domain.user.domain.repository.UserRepository;
import com.example.kwave.global.util.FloatArrayConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserPreferenceCacheService {

    private static final String KEY_PREFIX = "user:prefvec:";
    private static final long CACHE_TTL_SECONDS = 60 * 60 * 24;

    private final UserRepository userRepository;

    @Qualifier("userVectorRedisTemplate")
    private final RedisTemplate<String, byte[]> userVectorRedisTemplate;

    /**
     * Redis에서 선호 벡터 조회
     * 없을 시, MYSQL에서 로드 후 Redis에 캐싱
     * MYSQL에서도 null일 경우 null 반환
     */
    public float[] loadUserVector(UUID userId) {
        String key = KEY_PREFIX + userId;

        // Redis Hit
        byte[] cachedBytes = userVectorRedisTemplate.opsForValue().get(key);
        if (cachedBytes != null) {
            log.debug("Redis hit - userId: {}", userId);
            return FloatArrayConverter.toFloatArray(cachedBytes);
        }

        log.debug("Redis Miss - userId: {}, loading from MySQL", userId);

        // Redis Miss
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User Not Found: " + userId));

        byte[] prefBytes = user.getPreferenceVector();

        // MySQL에도 벡터가 없을 시
        if (prefBytes == null || prefBytes.length == 0) {
            log.debug("No Preference Vector in DB - userID: {}", userId);
            return null;
        }

        // Redis Caching
        userVectorRedisTemplate.opsForValue()
                .set(key, prefBytes, CACHE_TTL_SECONDS, TimeUnit.SECONDS);

        return FloatArrayConverter.toFloatArray(prefBytes);
    }

    /**
     * 배치로 선호벡터 재계산 뒤, Redis cache 업데이트
     */
    public void putUserVector(UUID userId, float[] vector) {
        if (vector == null) {
            evictUserVector(userId);
            return;
        }

        String key = KEY_PREFIX + userId;
        byte[] bytes = FloatArrayConverter.toBytes(vector);

        userVectorRedisTemplate.opsForValue()
                .set(key, bytes, CACHE_TTL_SECONDS, TimeUnit.SECONDS);

        log.debug("User vector cached in Redis - userId: {}", userId);
    }

    /**
     * 특정 유저 선호 벡터 캐시 지우고 싶을 때 사용
     * 새벽 배치 전 삭제 or 특정 유저 재계산 전
     */
    public void evictUserVector(UUID userId) {
        String key = KEY_PREFIX + userId;
        Boolean result = userVectorRedisTemplate.delete(key);
        log.debug("Evict Redis user vector - userId: {}, deleted: {}", userId, result);
    }
}
