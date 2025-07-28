package br.com.gazintech.orderapp.repository;

import br.com.gazintech.orderapp.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
}