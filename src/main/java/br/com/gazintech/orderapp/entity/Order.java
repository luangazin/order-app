package br.com.gazintech.orderapp.entity;

import br.com.gazintech.orderapp.dto.OrderPostRequestDTO;
import br.com.gazintech.orderapp.utils.OrderStatusStateMachine;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "\"Order\"", indexes = {
        @Index(name = "idx_partner_id", columnList = "partner_id"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
public class Order implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", nullable = false)
    private Partner partner;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<OrderItem> items;

    @Column(name = "total_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalValue = BigDecimal.ZERO;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Version
    private Long version;

    public Order(Partner partner, OrderPostRequestDTO orderDTO) {
        this.partner = partner;
        this.items = orderDTO.getItems().stream()
                .map(item -> OrderItem.builder()
                        .code(item.getCode())
                        .name(item.getName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
                .toList();
        calculateTotalValue();
        this.status = OrderStatus.PENDING;
    }

    public void calculateTotalValue() {
        this.totalValue = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // approve the order
    public void approve() {
        OrderStatusStateMachine.transitionTo(this, OrderStatus.APPROVED);
    }

    // process the order
    public void process() {
        OrderStatusStateMachine.transitionTo(this, OrderStatus.PROCESSING);
    }

    // ship the order
    public void ship() {
        OrderStatusStateMachine.transitionTo(this, OrderStatus.SHIPPED);
    }

    // deliver the order
    public void deliver() {
        OrderStatusStateMachine.transitionTo(this, OrderStatus.DELIVERED);
    }

    // cancel the order
    public void cancel() {
        OrderStatusStateMachine.transitionTo(this, OrderStatus.CANCELED);
    }

    /**
     * Enum representing the status of an order.
     * <p>
     * The order can be in one of the following states:
     * - PENDING: The order has been created but not yet approved.
     * - APPROVED: The order has been approved and is ready for processing.
     * - PROCESSING: The order is being processed.
     * - SHIPPED: The order has been shipped to the customer.
     * - DELIVERED: The order has been delivered to the customer.
     * - CANCELED: The order has been canceled.
     * </p>
     */
    public enum OrderStatus {
        PENDING,
        APPROVED,
        PROCESSING,
        SHIPPED,
        DELIVERED,
        CANCELED
    }
}
