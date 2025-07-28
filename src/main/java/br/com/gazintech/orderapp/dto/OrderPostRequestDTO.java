package br.com.gazintech.orderapp.dto;

import br.com.gazintech.orderapp.entity.Order;
import br.com.gazintech.orderapp.entity.OrderItem;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
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
public class OrderPostRequestDTO implements Serializable {

    @JsonProperty("partner-id")
    UUID partnerId;

    @JsonProperty("items")
    List<OrderItemDTO> items;

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
        @JsonProperty("code")
        String code;
        @JsonProperty("name")
        String name;
        @JsonProperty("quantity")
        Integer quantity;
        @JsonProperty("price")
        BigDecimal price;
    }
}