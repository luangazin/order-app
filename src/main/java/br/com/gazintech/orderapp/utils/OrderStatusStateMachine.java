package br.com.gazintech.orderapp.utils;

import br.com.gazintech.orderapp.entity.Order;
import br.com.gazintech.orderapp.exception.InvalidOrderStatusException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderStatusStateMachine {
    private static final Map<Order.OrderStatus, Set<Order.OrderStatus>> validTransitions = new EnumMap<>(Order.OrderStatus.class);

    static {
        // Define valid state transitions
        validTransitions.put(Order.OrderStatus.PENDING, new HashSet<>(Set.of(
                Order.OrderStatus.APPROVED,
                Order.OrderStatus.CANCELED
        )));

        validTransitions.put(Order.OrderStatus.APPROVED, new HashSet<>(Set.of(
                Order.OrderStatus.PROCESSING,
                Order.OrderStatus.CANCELED
        )));

        validTransitions.put(Order.OrderStatus.PROCESSING, new HashSet<>(Set.of(
                Order.OrderStatus.SHIPPED,
                Order.OrderStatus.CANCELED
        )));

        validTransitions.put(Order.OrderStatus.SHIPPED, new HashSet<>(Set.of(
                Order.OrderStatus.DELIVERED
        )));

        validTransitions.put(Order.OrderStatus.DELIVERED, new HashSet<>());
        validTransitions.put(Order.OrderStatus.CANCELED, new HashSet<>());
    }

    /**
     * Checks if a transition from current status to new status is valid
     *
     * @param currentStatus The current order status
     * @param newStatus     The proposed new status
     * @return true if the transition is valid, false otherwise
     */
    public static boolean isValidTransition(Order.OrderStatus currentStatus, Order.OrderStatus newStatus) {
        return validTransitions.getOrDefault(currentStatus, new HashSet<>()).contains(newStatus);
    }

    /**
     * Transitions the order to a new status if valid
     *
     * @param order     The order to transition
     * @param newStatus The desired new status
     * @throws IllegalStateException if the transition is invalid
     */
    public static void transitionTo(Order order, Order.OrderStatus newStatus) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        if (!isValidTransition(order.getStatus(), newStatus)) {
            throw new InvalidOrderStatusException(
                    String.format("Cannot transition from %s to %s", order.getStatus(), newStatus)
            );
        }
        order.setStatus(newStatus);
    }
}
