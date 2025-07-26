package br.com.gazintech.orderapp.idempotency.repository;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.io.Serializable;
import java.util.UUID;

@RedisHash(value = "idempotency-cache")
public class IdempotencyCache implements Serializable {
    @JsonProperty(value = "idempotency-key", required = true)
    private UUID idempotencyKey;

    @JsonProperty(value = "response", required = true)
    private final transient Object response;

    @TimeToLive
    private final Long expirationInSeconds;

    public IdempotencyCache(UUID idempotencyKey, Object response, Long expirationInSeconds) {
        this.idempotencyKey = idempotencyKey;
        this.response = response;
        this.expirationInSeconds = expirationInSeconds;
    }

    public UUID getIdempotencyKey() {
        return idempotencyKey;
    }

    public Object getResponse() {
        return response;
    }

    public Long getExpirationInSeconds() {
        return expirationInSeconds;
    }
}