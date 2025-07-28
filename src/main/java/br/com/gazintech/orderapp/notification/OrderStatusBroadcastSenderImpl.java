package br.com.gazintech.orderapp.notification;

import br.com.gazintech.orderapp.entity.Order;
import br.com.gazintech.orderapp.notification.dto.OrderStatusUpdateDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class OrderStatusBroadcastSenderImpl implements OrderStatusBroadcastSender {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${order-app.broadcast.update-status}")
    private String recipient;

    @Override
    public void sendNotification(UUID orderId, Order.OrderStatus oldStatus, Order.OrderStatus newStatus) {
        OrderStatusUpdateDTO build = OrderStatusUpdateDTO.builder()
                .orderId(orderId)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .build();
        try {
            redisTemplate.opsForList().leftPush(recipient, build);
            log.info("Message sent to queue: {}", recipient);
        } catch (Exception e) {
            log.error("Error sending message to queue: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send message to queue", e);
        }
    }
}
