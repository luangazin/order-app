package br.com.gazintech.orderapp.repository;

import br.com.gazintech.orderapp.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
}