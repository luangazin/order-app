package br.com.gazintech.orderapp.dto;

import br.com.gazintech.orderapp.entity.Order;
import br.com.gazintech.orderapp.entity.OrderItem;
import br.com.gazintech.orderapp.entity.Partner;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for {@link Order}
 */

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Getter
@Setter
public class OrderResponseDTO implements Serializable {
    UUID id;
    PartnerDTO partner;
    List<OrderItemDTO> items;
    BigDecimal totalValue;
    Order.OrderStatus status;
    OffsetDateTime createdAt;
    OffsetDateTime updatedAt;
    Long version;


    public OrderResponseDTO(Order order) {
        this.id = order.getId();
        this.partner = new PartnerDTO(order.getPartner());
        this.items = order.getItems().stream().map(OrderItemDTO::new).toList();
        this.totalValue = order.getTotalValue();
        this.status = order.getStatus();
        this.createdAt = order.getCreatedAt();
        this.updatedAt = order.getUpdatedAt();
        this.version = order.getVersion();
    }

    /**
     * DTO for {@link OrderItem}
     */
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @Builder
    @Getter
    @Setter
    public static class OrderItemDTO implements Serializable {
        UUID id;
        String code;
        String name;
        Integer quantity;
        BigDecimal price;

        public OrderItemDTO(OrderItem orderItem) {
            this.id = orderItem.getId();
            this.code = orderItem.getCode();
            this.name = orderItem.getName();
            this.quantity = orderItem.getQuantity();
            this.price = orderItem.getPrice();
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @Builder
    @Getter
    @Setter
    public static class PartnerDTO implements Serializable {
        UUID id;
        String name;

        public PartnerDTO(Partner partner) {
            this.id = partner.getId();
            this.name = partner.getName();
        }
    }


}