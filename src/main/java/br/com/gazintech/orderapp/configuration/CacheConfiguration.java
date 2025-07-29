package br.com.gazintech.orderapp.configuration;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.io.IOException;
import java.time.Duration;

@Configuration
public class CacheConfiguration {

    @Bean(name = "redisObjectMapper")
    public ObjectMapper redisObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder().allowIfBaseType(Object.class).build(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY // Use PROPERTY to avoid WRAPPER_ARRAY
        );
        return objectMapper;
    }

    @Primary
    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        return (builder) -> builder
                .withCacheConfiguration("orders",
                        RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(10)));
    }

    @Primary
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory,
                                     @Qualifier("redisObjectMapper") ObjectMapper redisObjectMapper) {

        RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new CustomJackson2JsonRedisSerializer(redisObjectMapper)
                ));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfiguration)
                .build();
    }
}

class CustomJackson2JsonRedisSerializer extends GenericJackson2JsonRedisSerializer {
    public CustomJackson2JsonRedisSerializer(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public <T> T deserialize(byte[] source, Class<T> type) throws SerializationException {
        try {
            return super.deserialize(source, type);
        } catch (SerializationException e) {
            // Fallback for legacy data without type information
            ObjectMapper fallbackMapper = new ObjectMapper();
            fallbackMapper.registerModule(new JavaTimeModule());
            try {
                return fallbackMapper.readValue(source, type);
            } catch (IOException ex) {
                throw new SerializationException("Failed to deserialize legacy data", ex);
            }
        }
    }
}