package br.com.gazintech.orderapp.configuration;

import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.core.RedisKeyValueTemplate;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.mapping.RedisMappingContext;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableRedisRepositories
public class RedisConfiguration {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(RedisConfiguration.class);

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionDetailsForRedis) {
        logger.info("Creating Redis connection details");
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionDetailsForRedis);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

    @Bean
    public RedisKeyValueAdapter redisKeyValueAdapter(RedisOperations<String, Object> redisOperations) {
        return new RedisKeyValueAdapter(redisOperations);
    }

    @Bean
    public RedisKeyValueTemplate redisKeyValueTemplate(RedisOperations<String, Object> redisOperations) {
        RedisKeyValueAdapter adapter = new RedisKeyValueAdapter(redisOperations);
        RedisMappingContext mappingContext = new RedisMappingContext();
        return new RedisKeyValueTemplate(adapter, mappingContext);
    }

    @Bean
    public RedisMappingContext redisMappingContext() {
        return new RedisMappingContext();
    }
}
