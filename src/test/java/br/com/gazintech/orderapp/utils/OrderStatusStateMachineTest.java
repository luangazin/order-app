package br.com.gazintech.orderapp.utils;

import br.com.gazintech.orderapp.entity.Order;
import br.com.gazintech.orderapp.exception.InvalidOrderStatusException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OrderStatusStateMachineTest {

    @Test
    void shouldThrowExceptionWhenOrderIsNull() {
        assertThrows(InvalidOrderStatusException.class,
                () -> OrderStatusStateMachine.transitionTo(null, Order.OrderStatus.APPROVED));
    }

    @Test
    void shouldThrowExceptionWhenTransitioningToSameStatus() {
        Order order = new Order();
        order.setStatus(Order.OrderStatus.PENDING);

        assertThrows(InvalidOrderStatusException.class,
                () -> OrderStatusStateMachine.transitionTo(order, Order.OrderStatus.PENDING));
    }

    @ParameterizedTest
    @MethodSource("validTransitionsProvider")
    void shouldAllowValidTransitions(Order.OrderStatus currentStatus, Order.OrderStatus newStatus) {
        Order order = new Order();
        order.setStatus(currentStatus);

        assertDoesNotThrow(() -> OrderStatusStateMachine.transitionTo(order, newStatus));
        assertEquals(newStatus, order.getStatus());
    }

    @ParameterizedTest
    @MethodSource("invalidTransitionsProvider")
    void shouldNotAllowInvalidTransitions(Order.OrderStatus currentStatus, Order.OrderStatus newStatus) {
        Order order = new Order();
        order.setStatus(currentStatus);

        assertThrows(InvalidOrderStatusException.class,
                () -> OrderStatusStateMachine.transitionTo(order, newStatus));
        assertEquals(currentStatus, order.getStatus());
    }

    private static Stream<Arguments> validTransitionsProvider() {
        return Stream.of(
                Arguments.of(Order.OrderStatus.PENDING, Order.OrderStatus.APPROVED),
                Arguments.of(Order.OrderStatus.PENDING, Order.OrderStatus.CANCELED),
                Arguments.of(Order.OrderStatus.APPROVED, Order.OrderStatus.PROCESSING),
                Arguments.of(Order.OrderStatus.APPROVED, Order.OrderStatus.CANCELED),
                Arguments.of(Order.OrderStatus.PROCESSING, Order.OrderStatus.SHIPPED),
                Arguments.of(Order.OrderStatus.PROCESSING, Order.OrderStatus.CANCELED),
                Arguments.of(Order.OrderStatus.SHIPPED, Order.OrderStatus.DELIVERED)
        );
    }

    private static Stream<Arguments> invalidTransitionsProvider() {
        return Stream.of(
                Arguments.of(Order.OrderStatus.PENDING, Order.OrderStatus.PROCESSING),
                Arguments.of(Order.OrderStatus.PENDING, Order.OrderStatus.DELIVERED),
                Arguments.of(Order.OrderStatus.APPROVED, Order.OrderStatus.DELIVERED),
                Arguments.of(Order.OrderStatus.PROCESSING, Order.OrderStatus.APPROVED),
                Arguments.of(Order.OrderStatus.SHIPPED, Order.OrderStatus.CANCELED),
                Arguments.of(Order.OrderStatus.DELIVERED, Order.OrderStatus.SHIPPED),
                Arguments.of(Order.OrderStatus.CANCELED, Order.OrderStatus.APPROVED)
        );
    }
}