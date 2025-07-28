package br.com.gazintech.orderapp.notification;


import br.com.gazintech.orderapp.entity.Order;

import java.util.UUID;

/**
 * Interface for sending notifications about order status changes.
 * This interface defines a method to send notifications when an order's status changes.
 */
public interface OrderStatusBroadcastSender {

    /**
     * Sends a notification about an order status change.
     *
     * @param orderId   The UUID of the order for which the status has changed.
     * @param oldStatus The previous status of the order.
     * @param newStatus The new status of the order.
     */
    void sendNotification(UUID orderId, Order.OrderStatus oldStatus, Order.OrderStatus newStatus);
}
