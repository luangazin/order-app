package br.com.gazintech.orderapp.dto;

import br.com.gazintech.orderapp.entity.Order;
import br.com.gazintech.orderapp.entity.OrderItem;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "Partner identifier", example = "1ffe19fd-cb50-4afe-b4cf-07aa691631df")
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

        @Schema(description = "item code", example = "0001")
        @JsonProperty("code")
        String code;

        @Schema(description = "item name", example = "item_001")
        @JsonProperty("name")
        String name;

        @Schema(description = "item quantity", example = "1")
        @JsonProperty("quantity")
        Integer quantity;

        @Schema(description = "item price", example = "9.99")
        @JsonProperty("price")
        BigDecimal price;
    }
}