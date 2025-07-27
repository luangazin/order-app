package br.com.gazintech.orderapp.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class BroadcastSenderImpl implements BroadcastSender {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void sendNotification(Object message, String recipient) {
        if (message == null || recipient == null || recipient.isEmpty()) {
            log.error("Message or recipient cannot be null or empty");
            throw new IllegalArgumentException("Message and recipient must not be null or empty");
        }
        try {
            redisTemplate.opsForList().leftPush(recipient, message);
            log.info("Message sent to queue: {}", message);
        } catch (Exception e) {
            log.error("Error sending message to queue: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send message to queue", e);
        }
    }
}
