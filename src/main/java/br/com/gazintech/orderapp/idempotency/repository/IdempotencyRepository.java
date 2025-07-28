package br.com.gazintech.orderapp.idempotency.repository;

import java.util.Optional;
import java.util.UUID;

public interface IdempotencyRepository {
    Optional<IdempotencyCache> getCache(UUID idempotencyKey);

    public void save(UUID idempotencyKey, IdempotencyCache idempotency);
}
