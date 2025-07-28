package br.com.gazintech.orderapp.controller;


import br.com.gazintech.orderapp.api.SortDirection;
import br.com.gazintech.orderapp.dto.OrderPostRequestDTO;
import br.com.gazintech.orderapp.dto.OrderResponseDTO;
import br.com.gazintech.orderapp.dto.OrderSearchDTO;
import br.com.gazintech.orderapp.entity.Order;
import br.com.gazintech.orderapp.idempotency.Idempotent;
import br.com.gazintech.orderapp.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Tag(name = "Order Management", description = "Operations related to order management")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/v1/orders")
@RestController
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Create new Order", description = "Creates a new order for a partner")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request, invalid input"),
            @ApiResponse(responseCode = "404", description = "Not found, partner not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @Idempotent(cacheTimeSeconds = 3600)
    @PostMapping
    public ResponseEntity<br.com.gazintech.orderapp.api.ApiResponse<OrderResponseDTO>> createOrder(
            @Parameter(name = "X-Idempotency-Key", description = "Idempotency key to safely retry requests", in = ParameterIn.HEADER, required = false, schema = @Schema(type = "string", format = "uuid"))
            @RequestHeader(name = "X-Idempotency-Key", required = true) UUID idempotencyKey,
            @Validated @RequestBody OrderPostRequestDTO orderPostRequestDTO) {
        log.info("Creating new order", kv("idempotencyKey", idempotencyKey), kv("body", orderPostRequestDTO));
        log.debug("Received request to create order with idempotency key: {}", idempotencyKey);
        return br.com.gazintech.orderapp.api.ApiResponse.<OrderResponseDTO>builder()
                .success()
                .body(orderService.createOrder(orderPostRequestDTO))
                .httpStatus(HttpStatus.CREATED)
                .build();
    }

    @Operation(summary = "Search orders", description = "Searches for orders with optional filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order list found")
    })
    @GetMapping
    public ResponseEntity<br.com.gazintech.orderapp.api.ApiResponse<List<OrderResponseDTO>>> searchOrders(
            @Parameter(description = "Order ID") @RequestParam(name = "order-id", required = false) UUID orderId,
            @Parameter(description = "Partner ID") @RequestParam(name = "partner-id", required = false) UUID partnerId,
            @Parameter(description = "Order Status") @RequestParam(name = "status", required = false) Order.OrderStatus status,
            @Parameter(description = "Start date") @RequestParam(name = "start-date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) OffsetDateTime startDate,
            @Parameter(description = "End date") @RequestParam(name = "end-date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) OffsetDateTime endDate,
            @Parameter(description = "Page number") @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(name = "size", defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(name = "sort-by", defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(name = "sort-direction", defaultValue = "DESC") SortDirection sortDirection) {


        log.info("Searching orders with filters",
                kv("status", status), kv("startDate", startDate), kv("endDate", endDate));

        OrderSearchDTO searchDTO = OrderSearchDTO.builder()
                .orderId(orderId)
                .partnerId(partnerId)
                .status(status)
                .startDate(startDate)
                .endDate(endDate)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        Page<OrderResponseDTO> pageResult = orderService.searchOrders(searchDTO);

        return br.com.gazintech.orderapp.api.ApiResponse.<List<OrderResponseDTO>>builder()
                .success()
                .body(pageResult.getContent())
                .pagination(pageResult)
                .build();
    }

    @Operation(summary = "Find order by id", description = "Returns an order by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order found"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<br.com.gazintech.orderapp.api.ApiResponse<OrderResponseDTO>> getOrderById(
            @Parameter(description = "Order id") @PathVariable UUID id) {
        log.info("Fetching order by ID", kv("orderId", id));

        return br.com.gazintech.orderapp.api.ApiResponse.<OrderResponseDTO>builder()
                .success()
                .body(orderService.getOrderById(id))
                .build();
    }

    @Operation(summary = "Patches order's status", description = "Patches an order's status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request, invalid status"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<br.com.gazintech.orderapp.api.ApiResponse<OrderResponseDTO>> updateOrderStatus(
            @Parameter(description = "Order id") @PathVariable UUID id,
            @Parameter(description = "New status", required = true) @RequestParam Order.OrderStatus status) {

        log.info("Updating order status", kv("orderId", id), kv("newStatus", status));

        return br.com.gazintech.orderapp.api.ApiResponse.<OrderResponseDTO>builder()
                .success()
                .body(orderService.updateOrderStatus(id, status))
                .build();
    }

    @Operation(summary = "Cancel order", description = "Cancels a specific order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Order canceled successfully"),
            @ApiResponse(responseCode = "400", description = "Order cannot be canceled"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelOrder(
            @Parameter(description = "ID do pedido") @PathVariable UUID id) {
        log.info("Canceling order", kv("orderId", id));
        orderService.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }
}
