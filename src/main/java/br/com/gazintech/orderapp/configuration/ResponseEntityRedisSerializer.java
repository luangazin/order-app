package br.com.gazintech.orderapp.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

public class ResponseEntityRedisSerializer implements RedisSerializer<ResponseEntity<?>> {
    private final ObjectMapper objectMapper;

    public ResponseEntityRedisSerializer() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public byte[] serialize(ResponseEntity<?> responseEntity) throws SerializationException {
        try {
            return objectMapper.writeValueAsString(responseEntity).getBytes();
        } catch (JsonProcessingException e) {
            throw new SerializationException("Error serializing ResponseEntity", e);
        }
    }

    @Override
    public ResponseEntity<?> deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null) {
            return null;
        }
        try {
            return objectMapper.readValue(bytes, ResponseEntity.class);
        } catch (JsonProcessingException e) {
            throw new SerializationException("Error deserializing ResponseEntity", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}