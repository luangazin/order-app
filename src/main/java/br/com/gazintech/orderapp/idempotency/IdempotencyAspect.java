package br.com.gazintech.orderapp.idempotency;

import br.com.gazintech.orderapp.exception.IdempotencyKeyNotFoundException;
import br.com.gazintech.orderapp.idempotency.repository.IdempotencyCache;
import br.com.gazintech.orderapp.idempotency.repository.IdempotencyRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;
import java.util.UUID;

@Aspect
@Component
public class IdempotencyAspect {

    private static final Logger logger = LoggerFactory.getLogger(IdempotencyAspect.class);

    @Autowired
    private IdempotencyRepository repository; // Inject the Redis template

    @Around("@annotation(Idempotent) && execution(* *(..))")
    public Object handleIdempotency(ProceedingJoinPoint joinPoint) throws Throwable {
        logger.trace("Idempotency aspect triggered");
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Idempotent idempotent = methodSignature.getMethod().getAnnotation(Idempotent.class);
        String idempotencyKeyHeader = this.getIdempotencyKeyHeader(idempotent);
        UUID idempotencyKey = UUID.fromString(idempotencyKeyHeader);

        logger.trace("Idempotency-Key: {}", idempotencyKey);
        Optional<IdempotencyCache> cacheItem = repository.getCache(idempotencyKey);
        if (cacheItem.isPresent()) {
            logger.trace("Idempotency-Key found in cache: {}, return response", idempotencyKey);
            return cacheItem.get().getResponse();
        }

        logger.trace("Idempotency-Key not found in cache: {}, proceed with method execution", idempotencyKey);
        Object result = joinPoint.proceed();

        repository.save(idempotencyKey, new IdempotencyCache(idempotencyKey, result, idempotent.cacheTimeSeconds()));

        logger.trace("Idempotency-Key saved in cache: {}.", idempotencyKey);
        return result;
    }

    private String getIdempotencyKeyHeader(Idempotent idempotent) {
        if (idempotent == null) {
            throw new IllegalStateException("Idempotent annotation not found");
        }
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new IllegalStateException("No request attributes found");
        }
        var request = attributes.getRequest();

        String idempotencyKeyHeader = request.getHeader("Idempotency-Key");
        if (idempotencyKeyHeader == null) {
            throw new IdempotencyKeyNotFoundException("Idempotency-Key header is required");
        }
        return idempotencyKeyHeader;
    }
}
