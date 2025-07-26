package br.com.gazintech.orderapp.idempotency.repository;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisKeyValueTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class IdempotencyRepositoryImpl implements IdempotencyRepository {
    private static final Logger logger = LoggerFactory.getLogger(IdempotencyRepositoryImpl.class);

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
        logger.debug("Fetching idempotency key from Redis: {}", redisKey);
        return redisTemplate.findById(redisKey, IdempotencyCache.class);
    }

    /**
     * Saves the idempotency key in Redis.
     */
    @Override
    public void save(UUID idempotencyKey, IdempotencyCache idempotency) {
        String redisKey = getRedisKey(idempotencyKey);
        logger.debug("Creating idempotency key in Redis: {} with TTL: {}", redisKey, idempotency.getExpirationInSeconds());
        redisTemplate.update(redisKey, idempotency);
    }

    /**
     * Generates the Redis key for the idempotency key.
     */
    private String getRedisKey(UUID idempotencyKey) {
        return "idempotency:" + idempotencyKey.toString();
    }
}
