package br.com.gazintech.orderapp.controller;

import br.com.gazintech.orderapp.AbstractIntegrationTest;
import br.com.gazintech.orderapp.api.ApiResponse;
import br.com.gazintech.orderapp.dto.OrderPostRequestDTO;
import br.com.gazintech.orderapp.entity.Order;
import br.com.gazintech.orderapp.entity.OrderItem;
import br.com.gazintech.orderapp.entity.Partner;
import br.com.gazintech.orderapp.exception.InvalidOrderStatusException;
import br.com.gazintech.orderapp.exception.OrderNotFoundException;
import br.com.gazintech.orderapp.exception_handler.ExceptionHandlerItem;
import br.com.gazintech.orderapp.exception_handler.ExceptionHandlerList;
import br.com.gazintech.orderapp.repository.OrderRepository;
import br.com.gazintech.orderapp.repository.PartnerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.context.WebApplicationContext;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureWebMvc
@AutoConfigureMockMvc
@Transactional
@DisplayName("Order Controller Integration Tests")
class OrderControllerTest extends AbstractIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PartnerRepository partnerRepository;

    private Partner partnerCreated;

    @Autowired
    private ExceptionHandlerList handlerList;

    @Autowired
    private OrderRepository orderRepository;


    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        Partner partner = new Partner();
        partner.setName("Test Partner");
        partner.setAvailableCredit(BigDecimal.valueOf(1000.00));
        partner.setCreditLimit(BigDecimal.valueOf(2000.00));
        partner.setCode("0001");
        partner.setEmail("partner01@mock.com");
        partner.setActive(true);
        partnerCreated = partnerRepository.save(partner);
    }


    @Nested
    @DisplayName("POST /v1/orders - Create Order")
    class CreateOrderTests {

        @Test
        @DisplayName("Should create order successfully with valid data")
        void shouldCreateOrderSuccessfully() throws Exception {
            UUID partnerId = partnerCreated.getId();
            UUID idempotencyKey = UUID.randomUUID();

            OrderPostRequestDTO orderPostRequestDTO = OrderPostRequestDTO.builder()
                    .partnerId(partnerId)
                    .items(List.of(
                            OrderPostRequestDTO.OrderItemDTO.builder().name("Item 1").code("001").quantity(2).price(BigDecimal.valueOf(5.54)).build(),
                            OrderPostRequestDTO.OrderItemDTO.builder().name("Item 2").code("002").quantity(2).price(BigDecimal.valueOf(8.57)).build(),
                            OrderPostRequestDTO.OrderItemDTO.builder().name("Item 3").code("003").quantity(2).price(BigDecimal.valueOf(2.99)).build(),
                            OrderPostRequestDTO.OrderItemDTO.builder().name("Item 4").code("004").quantity(2).price(BigDecimal.valueOf(15.89)).build()
                    ))
                    .build();

            BigDecimal total = orderPostRequestDTO.getItems().stream()
                    .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity().longValue())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);


            mockMvc.perform(post("/v1/orders")
                            .header("X-Idempotency-Key", idempotencyKey.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(orderPostRequestDTO)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status", is(ApiResponse.Status.SUCCESS.name())))
                    .andExpect(jsonPath("$.body.partner.id", is(partnerId.toString())))
                    .andExpect(jsonPath("$.body.status", is(Order.OrderStatus.PENDING.name())))
                    .andExpect(jsonPath("$.body.totalValue", is(total.doubleValue())));


        }

        @Test
        @DisplayName("Should return 400 when idempotency key is missing")
        void shouldReturn400WhenIdempotencyKeyMissing() throws Exception {
            UUID partnerId = partnerCreated.getId();

            OrderPostRequestDTO orderPostRequestDTO = OrderPostRequestDTO.builder()
                    .partnerId(partnerId)
                    .items(List.of(
                            OrderPostRequestDTO.OrderItemDTO.builder().name("Item 1").code("001").quantity(2).price(BigDecimal.valueOf(5.54)).build(),
                            OrderPostRequestDTO.OrderItemDTO.builder().name("Item 2").code("002").quantity(2).price(BigDecimal.valueOf(8.57)).build(),
                            OrderPostRequestDTO.OrderItemDTO.builder().name("Item 3").code("003").quantity(2).price(BigDecimal.valueOf(2.99)).build(),
                            OrderPostRequestDTO.OrderItemDTO.builder().name("Item 4").code("004").quantity(2).price(BigDecimal.valueOf(15.89)).build()
                    ))
                    .build();

            ExceptionHandlerItem exception = handlerList.findByException(new MissingRequestHeaderException("", null));

            mockMvc.perform(post("/v1/orders")
                            .header("X-Idempotency-Key", "")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(orderPostRequestDTO)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error-code", is(exception.errorCode())))
                    .andExpect(jsonPath("$.status", is(ApiResponse.Status.ERROR.name())));


        }

        @Test
        @DisplayName("Should return 400 when request body is invalid")
        void shouldReturn400WhenRequestBodyInvalid() throws Exception {
            UUID idempotencyKey = UUID.randomUUID();
            OrderPostRequestDTO invalidRequest = OrderPostRequestDTO.builder().build();

            ExceptionHandlerItem exception = handlerList.findByException(new InvalidDataAccessApiUsageException("", null));
            // When & Then
            mockMvc.perform(post("/v1/orders")
                            .header("X-Idempotency-Key", idempotencyKey.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(ApiResponse.Status.ERROR.name())))
                    .andExpect(jsonPath("$.error-code", is(exception.errorCode())));

        }
    }

    @Nested
    @DisplayName("GET /v1/orders - Search Orders")
    class SearchOrdersTests {

        @Test
        @DisplayName("Should search orders with no filters")
        void shouldSearchOrdersWithNoFilters() throws Exception {
            OrderItem item = new OrderItem();
            item.setCode("001");
            item.setName("Test Item");
            item.setQuantity(2);
            item.setPrice(BigDecimal.valueOf(10.00));

            Order orderTest = Order.builder()
                    .partner(partnerCreated)
                    .items(List.of(item))
                    .totalValue(BigDecimal.valueOf(10.00))
                    .status(Order.OrderStatus.PENDING)
                    .build();
            item.setOrder(orderTest);
            Order saved = orderRepository.save(orderTest);


            // When & Then
            mockMvc.perform(get("/v1/orders"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is(ApiResponse.Status.SUCCESS.name())))
                    .andExpect(jsonPath("$.body", hasSize(1)))
                    .andExpect(jsonPath("$.body[0].id", is(saved.getId().toString())))
                    .andExpect(jsonPath("$.pagination.total-items", is(1)));

        }

        @Test
        @DisplayName("Should search orders with all filters")
        void shouldSearchOrdersWithAllFilters() throws Exception {

            OrderItem item = new OrderItem();
            item.setCode("001");
            item.setName("Test Item");
            item.setQuantity(2);
            item.setPrice(BigDecimal.valueOf(10.00));

            Order orderTest = Order.builder()
                    .partner(partnerCreated)
                    .items(List.of(item))
                    .totalValue(BigDecimal.valueOf(10.00))
                    .status(Order.OrderStatus.PENDING)
                    .build();

            item.setOrder(orderTest);
            Order saved = orderRepository.save(orderTest);

            OffsetDateTime startDate = OffsetDateTime.now().minusDays(7);
            OffsetDateTime endDate = OffsetDateTime.now();

            mockMvc.perform(get("/v1/orders")
                            .param("orderId-id", saved.getId().toString())
                            .param("partner-id", partnerCreated.getId().toString())
                            .param("status", "PENDING")
                            .param("start-date", startDate.toString())
                            .param("end-date", endDate.toString())
                            .param("page", "0")
                            .param("size", "10")
                            .param("sort-by", "createdAt")
                            .param("sort-direction", "DESC"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is(ApiResponse.Status.SUCCESS.name())))
                    .andExpect(jsonPath("$.body", hasSize(1)));


        }

        @Test
        @DisplayName("Should return empty list when no orders found")
        void shouldReturnEmptyListWhenNoOrdersFound() throws Exception {
            mockMvc.perform(get("/v1/orders"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is(ApiResponse.Status.SUCCESS.name())))
                    .andExpect(jsonPath("$.body", hasSize(0)))
                    .andExpect(jsonPath("$.pagination.total-items", is(0)));
        }
    }

    @Nested
    @DisplayName("GET /v1/orders/{id} - Get Order By ID")
    class GetOrderByIdTests {

        @Test
        @DisplayName("Should get order by ID successfully")
        void shouldGetOrderByIdSuccessfully() throws Exception {
            OrderItem item = new OrderItem();
            item.setCode("001");
            item.setName("Test Item");
            item.setQuantity(2);
            item.setPrice(BigDecimal.valueOf(10.00));

            Order orderTest = Order.builder()
                    .partner(partnerCreated)
                    .items(List.of(item))
                    .totalValue(BigDecimal.valueOf(10.00))
                    .status(Order.OrderStatus.PENDING)
                    .build();

            item.setOrder(orderTest);

            Order saved = orderRepository.save(orderTest);

            mockMvc.perform(get("/v1/orders/{id}", saved.getId()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is(ApiResponse.Status.SUCCESS.name())))
                    .andExpect(jsonPath("$.body.id", is(saved.getId().toString())))
                    .andExpect(jsonPath("$.body.partner.id", is(partnerCreated.getId().toString())))
                    .andExpect(jsonPath("$.body.status", is(Order.OrderStatus.PENDING.name())));

        }

        @Test
        @DisplayName("Should return 404 when order not found")
        void shouldReturn404WhenOrderNotFound() throws Exception {
            UUID nonExistentId = UUID.randomUUID();
            ExceptionHandlerItem exception = handlerList.findByException(new OrderNotFoundException(""));
            mockMvc.perform(get("/v1/orders/{id}", nonExistentId))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status", is(ApiResponse.Status.ERROR.name())))
                    .andExpect(jsonPath("$.error-code", is(exception.errorCode())));


        }
    }

    @Nested
    @DisplayName("PATCH /v1/orders/{id}/status - Update Order Status")
    class UpdateOrderStatusTests {

        @Test
        @DisplayName("Should update order status successfully")
        void shouldUpdateOrderStatusSuccessfully() throws Exception {
            OrderItem item = new OrderItem();
            item.setCode("001");
            item.setName("Test Item");
            item.setQuantity(2);
            item.setPrice(BigDecimal.valueOf(10.00));

            Order orderTest = Order.builder()
                    .partner(partnerCreated)
                    .items(List.of(item))
                    .totalValue(BigDecimal.valueOf(10.00))
                    .status(Order.OrderStatus.PENDING)
                    .build();

            item.setOrder(orderTest);

            Order saved = orderRepository.save(orderTest);


            mockMvc.perform(patch("/v1/orders/{id}/status", saved.getId())
                            .param("status", Order.OrderStatus.APPROVED.name()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is(ApiResponse.Status.SUCCESS.name())))
                    .andExpect(jsonPath("$.body.id", is(saved.getId().toString())))
                    .andExpect(jsonPath("$.body.status", is(Order.OrderStatus.APPROVED.name())))
                    .andExpect(jsonPath("$.body.partner.id", is(partnerCreated.getId().toString())));


        }

        @Test
        @DisplayName("Should return 400 when status parameter is missing")
        void shouldReturn400WhenStatusParameterMissing() throws Exception {
            UUID orderId = UUID.randomUUID();
            ExceptionHandlerItem exception = handlerList.findByException(new MissingServletRequestParameterException("", null));
            mockMvc.perform(patch("/v1/orders/{id}/status", orderId))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(ApiResponse.Status.ERROR.name())))
                    .andExpect(jsonPath("$.error-code", is(exception.errorCode())));

        }
    }

    @Nested
    @DisplayName("DELETE /v1/orders/{id} - Cancel Order")
    class CancelOrderTests {

        @Test
        @DisplayName("Should cancel order successfully")
        void shouldCancelOrderSuccessfully() throws Exception {
            OrderItem item = new OrderItem();
            item.setCode("001");
            item.setName("Test Item");
            item.setQuantity(2);
            item.setPrice(BigDecimal.valueOf(10.00));

            Order orderTest = Order.builder()
                    .partner(partnerCreated)
                    .items(List.of(item))
                    .totalValue(BigDecimal.valueOf(10.00))
                    .status(Order.OrderStatus.PENDING)
                    .build();

            item.setOrder(orderTest);
            Order saved = orderRepository.save(orderTest);

            mockMvc.perform(delete("/v1/orders/{id}", saved.getId()))
                    .andDo(print())
                    .andExpect(status().isNoContent());
            Optional<Order> orderOptional = orderRepository.findById(saved.getId());
            assert orderOptional.isPresent();
            assert orderOptional.get().getStatus() == Order.OrderStatus.CANCELED;
        }

        @Test
        @DisplayName("Should return 404 when trying to cancel non-existent order")
        void shouldReturn404WhenTryingToCancelNonExistentOrder() throws Exception {
            // Given
            UUID nonExistentId = UUID.randomUUID();

            ExceptionHandlerItem exception = handlerList.findByException(new OrderNotFoundException(""));

            mockMvc.perform(delete("/v1/orders/{id}", nonExistentId))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status", is(ApiResponse.Status.ERROR.name())))
                    .andExpect(jsonPath("$.error-code", is(exception.errorCode())));
        }

        @Test
        @DisplayName("Should return 400 when order cannot be canceled")
        void shouldReturn400WhenOrderCannotBeCanceled() throws Exception {
            OrderItem item = new OrderItem();
            item.setCode("001");
            item.setName("Test Item");
            item.setQuantity(2);
            item.setPrice(BigDecimal.valueOf(10.00));

            Order orderTest = Order.builder()
                    .partner(partnerCreated)
                    .items(List.of(item))
                    .totalValue(BigDecimal.valueOf(10.00))
                    .status(Order.OrderStatus.CANCELED)
                    .build();

            item.setOrder(orderTest);
            Order saved = orderRepository.save(orderTest);
            ExceptionHandlerItem exception = handlerList.findByException(new InvalidOrderStatusException(""));
            mockMvc.perform(delete("/v1/orders/{id}", saved.getId()))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(ApiResponse.Status.ERROR.name())))
                    .andExpect(jsonPath("$.error-code", is(exception.errorCode())));


        }
    }
}