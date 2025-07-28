package br.com.gazintech.orderapp.idempotency.repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Interface for managing idempotency cache.
 * It provides methods to retrieve and save idempotency cache entries.
 */
public interface IdempotencyRepository {

    /**
     * Retrieves an idempotency cache entry by its key.
     *
     * @param idempotencyKey The UUID representing the idempotency key.
     * @return An Optional containing the IdempotencyCache if found, otherwise empty.
     */
    Optional<IdempotencyCache> getCache(UUID idempotencyKey);

    /**
     * Saves an idempotency cache entry with the given key.
     *
     * @param idempotencyKey The UUID representing the idempotency key.
     * @param idempotency    The IdempotencyCache object to be saved.
     */
    public void save(UUID idempotencyKey, IdempotencyCache idempotency);
}
