package br.com.gazintech.orderapp.service;

import br.com.gazintech.orderapp.entity.Order;
import br.com.gazintech.orderapp.entity.Partner;
import br.com.gazintech.orderapp.notification.OrderStatusBroadcastSender;
import br.com.gazintech.orderapp.repository.PartnerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final PartnerRepository partnerRepository;
    private final OrderStatusBroadcastSender broadcastSender;

    @Async
    public void notifyOrderStatusChange(Order order, Order.OrderStatus previousStatus) {
        log.info("Sending notification for order {} status change from {} to {}",
                order.getId(), previousStatus, order.getStatus());

        try {
            Partner partner = partnerRepository.findById(order.getPartner().getId())
                    .orElse(null);

            if (partner == null) {
                log.warn("Partner not found for order notification: {}", order.getPartner().getId());
                return;
            }

            broadcastSender.sendNotification(order.getId(), previousStatus, order.getStatus());

            log.info("Notification sent successfully for order {} to partner {}",
                    order.getId(), partner.getEmail());

        } catch (Exception e) {
            log.error("Failed to send notification for order {}: {}", order.getId(), e.getMessage(), e);
        }
    }
}
