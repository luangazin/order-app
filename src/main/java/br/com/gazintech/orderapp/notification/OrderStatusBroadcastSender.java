package br.com.gazintech.orderapp.notification;


import br.com.gazintech.orderapp.entity.Order;

import java.util.UUID;

public interface OrderStatusBroadcastSender {

    /**
     * Sends a notification about an order status change.
     *
     * @param orderId
     * @param oldStatus
     * @param newStatus
     */
    void sendNotification(UUID orderId, Order.OrderStatus oldStatus, Order.OrderStatus newStatus);
}
