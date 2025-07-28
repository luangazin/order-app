package br.com.gazintech.orderapp.idempotency.repository;


import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisKeyValueTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of the IdempotencyRepository interface using Redis as the storage mechanism.
 * This class provides methods to retrieve and save idempotency cache entries.
 */
@Slf4j
@Component
public class IdempotencyRepositoryImpl implements IdempotencyRepository {

    private final RedisKeyValueTemplate redisTemplate;

    @Autowired
    public IdempotencyRepositoryImpl(RedisKeyValueTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Fetches the idempotency key from Redis.
     */
    @Override
    public Optional<IdempotencyCache> getCache(UUID idempotencyKey) {
        String redisKey = getRedisKey(idempotencyKey);
        log.debug("Fetching idempotency key from Redis: {}", redisKey);
        return  redisTemplate.findById(redisKey, IdempotencyCache.class);
    }

    /**
     * Saves the idempotency key in Redis.
     */
    @Override
    public void save(UUID idempotencyKey, IdempotencyCache idempotency) {
        String redisKey = getRedisKey(idempotencyKey);
        log.debug("Creating idempotency key in Redis: {} with TTL: {}", redisKey, idempotency.getExpirationInSeconds());
        redisTemplate.update(redisKey, idempotency);
    }

    /**
     * Generates the Redis key for the idempotency key.
     */
    private String getRedisKey(UUID idempotencyKey) {
        return "idempotency:" + idempotencyKey.toString();
    }
}
