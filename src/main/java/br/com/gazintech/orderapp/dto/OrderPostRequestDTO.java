package br.com.gazintech.orderapp.dto;

import br.com.gazintech.orderapp.entity.Order;
import br.com.gazintech.orderapp.entity.OrderItem;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
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

    @NotNull(message = "Partner ID is required")
    @JsonProperty("partner-id")
    @Schema(description = "Partner identifier", example = "1ffe19fd-cb50-4afe-b4cf-07aa691631df")
    UUID partnerId;

    @NotNull(message = "Items list is required")
    @NotEmpty(message = "Items list cannot be empty")
    @Valid
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

        @NotBlank(message = "Item code is required")
        @Size(max = 50, message = "Item code cannot exceed 50 characters")
        @Schema(description = "item code", example = "0001")
        @JsonProperty("code")
        String code;

        @NotBlank(message = "Item name is required")
        @Size(max = 255, message = "Item name cannot exceed 255 characters")
        @Schema(description = "item name", example = "item_001")
        @JsonProperty("name")
        String name;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        @Max(value = 9999, message = "Quantity cannot exceed 9999")
        @Schema(description = "item quantity", example = "1")
        @JsonProperty("quantity")
        Integer quantity;

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        @Digits(integer = 10, fraction = 2, message = "Price must have at most 10 integer digits and 2 decimal places")
        @Schema(description = "item price", example = "9.99")
        @JsonProperty("price")
        BigDecimal price;
    }
}