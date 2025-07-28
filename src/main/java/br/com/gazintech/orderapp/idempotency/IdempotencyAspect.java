package br.com.gazintech.orderapp.idempotency;

import br.com.gazintech.orderapp.exception.IdempotencyKeyNotFoundException;
import br.com.gazintech.orderapp.idempotency.repository.IdempotencyCache;
import br.com.gazintech.orderapp.idempotency.repository.IdempotencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;
import java.util.UUID;

/**
 * Aspect for handling idempotency in methods annotated with @Idempotent.
 * It checks if the request has an idempotency key and manages the cache accordingly.
 */
@RequiredArgsConstructor
@Slf4j
@Aspect
@Component
public class IdempotencyAspect {

    private final IdempotencyRepository repository;

    /**
     * Intercepts methods annotated with @Idempotent and handles idempotency logic.
     * It checks if the idempotency key exists in the cache and returns the cached response if available.
     * If not, it proceeds with the method execution and saves the response in the cache.
     *
     * @param joinPoint The join point representing the method execution.
     * @return The response from the method or cached response if available.
     * @throws Throwable If an error occurs during method execution.
     */
    @Around("@annotation(Idempotent) && execution(* *(..))")
    public Object handleIdempotency(ProceedingJoinPoint joinPoint) throws Throwable {
        log.trace("Idempotency aspect triggered");
        // Retrieve the method signature and the Idempotent annotation
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Idempotent idempotent = methodSignature.getMethod().getAnnotation(Idempotent.class);
        String idempotencyKeyHeader = this.getIdempotencyKeyHeader(idempotent);
        UUID idempotencyKey = UUID.fromString(idempotencyKeyHeader);

        log.trace("X-Idempotency-Key: {}", idempotencyKey);
        Optional<IdempotencyCache> cacheItem = repository.getCache(idempotencyKey);
        if (cacheItem.isPresent()) {
            IdempotencyCache.ResponseData responseData = cacheItem.get().getResponse();
            log.trace("X-Idempotency-Key found in cache: {}, return response", idempotencyKey);
            return ResponseEntity.status(responseData.getStatus())
                    .body(responseData.getBody());
        }

        log.trace("X-Idempotency-Key not found in cache: {}, proceed with method execution", idempotencyKey);
        Object result = joinPoint.proceed();
        if (result instanceof ResponseEntity<?> responseEntity) {
            IdempotencyCache.ResponseData responseData = new IdempotencyCache.ResponseData(
                    responseEntity.getBody(),
                    responseEntity.getStatusCode()
            );
            log.trace("Saving X-Idempotency-Key in cache: {}, response: {}", idempotencyKey, responseData);
            repository.save(idempotencyKey, new IdempotencyCache(idempotencyKey, responseData, idempotent.cacheTimeSeconds()));
        }

        log.trace("X-Idempotency-Key saved in cache: {}.", idempotencyKey);
        return result;
    }

    /**
     * Retrieves the idempotency key from the request header.
     * Throws an exception if the header is not found or if the request attributes are not available.
     *
     * @param idempotent The Idempotent annotation from the method.
     * @return The idempotency key as a String.
     */
    private String getIdempotencyKeyHeader(Idempotent idempotent) {
        if (idempotent == null) {
            throw new IllegalStateException("Idempotent annotation not found");
        }
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new IllegalStateException("No request attributes found");
        }
        var request = attributes.getRequest();

        String idempotencyKeyHeader = request.getHeader("X-Idempotency-Key");
        if (idempotencyKeyHeader == null) {
            throw new IdempotencyKeyNotFoundException("X-Idempotency-Key header is required");
        }
        return idempotencyKeyHeader;
    }
}
