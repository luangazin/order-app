package br.com.gazintech.orderapp.idempotency.repository;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.http.HttpStatusCode;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "idempotency-cache")
public class IdempotencyCache implements Serializable {

    @JsonProperty(value = "idempotency-key", required = true)
    private UUID idempotencyKey;

    @JsonProperty(value = "response", required = true)
    private ResponseData response;

    @TimeToLive
    private Long expirationInSeconds;


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseData implements Serializable {
        private Object body;
        private HttpStatusCode status;
    }
}

