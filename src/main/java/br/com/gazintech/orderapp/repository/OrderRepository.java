package br.com.gazintech.orderapp.repository;

import br.com.gazintech.orderapp.dto.OrderSearchDTO;
import br.com.gazintech.orderapp.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    @Query("""
            SELECT o FROM Order o
              JOIN o.items it
              JOIN o.partner
            WHERE (:#{#dto.orderId} IS NULL OR o.id = :#{#dto.orderId})
              AND (:#{#dto.partnerId} IS NULL OR o.partner.id = :#{#dto.partnerId})
              AND (:#{#dto.startDate} IS NULL OR :#{#dto.endDate} IS NULL OR o.createdAt BETWEEN :#{#dto.startDate} AND :#{#dto.endDate})
              AND (:#{#dto.status} IS NULL OR o.status = :#{#dto.status})
            """)
    Page<Order> searchOrders(@Param("dto") OrderSearchDTO dto, Pageable pageable);
}