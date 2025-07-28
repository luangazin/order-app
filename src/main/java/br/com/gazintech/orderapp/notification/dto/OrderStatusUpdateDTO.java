package br.com.gazintech.orderapp.notification.dto;


import br.com.gazintech.orderapp.entity.Order;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Builder
@Data
public class OrderStatusUpdateDTO implements Serializable {
    @JsonProperty("order-id")
    private UUID orderId;

    @JsonProperty("old-status")
    private Order.OrderStatus oldStatus;

    @JsonProperty("new-status")
    private Order.OrderStatus newStatus;
}
