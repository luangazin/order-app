package br.com.gazintech.orderapp.service;


import br.com.gazintech.orderapp.dto.OrderPostRequestDTO;
import br.com.gazintech.orderapp.dto.OrderResponseDTO;
import br.com.gazintech.orderapp.dto.OrderSearchDTO;
import br.com.gazintech.orderapp.entity.Order;
import br.com.gazintech.orderapp.entity.Partner;
import br.com.gazintech.orderapp.exception.OrderNotFoundException;
import br.com.gazintech.orderapp.exception.PartnerNotFoundException;
import br.com.gazintech.orderapp.repository.OrderRepository;
import br.com.gazintech.orderapp.repository.PartnerRepository;
import br.com.gazintech.orderapp.repository.specification.OrderSpecifications;
import br.com.gazintech.orderapp.utils.OrderStatusStateMachine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service for managing orders.
 * Provides methods to create, retrieve, update, and search orders.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final PartnerRepository partnerRepository;
    private final CreditService creditService;
    private final NotificationService notificationService;

    /**
     * Creates a new order for the specified partner.
     *
     * @param orderDTO the order details
     * @return the created order response
     * @throws PartnerNotFoundException if the partner does not exist
     */
    @Transactional
    public OrderResponseDTO createOrder(OrderPostRequestDTO orderDTO)  throws PartnerNotFoundException {
        log.info("Creating order for partner: {}", orderDTO.getPartnerId());

        Partner partner = partnerRepository.findById(orderDTO.getPartnerId())
                .orElseThrow(() -> new PartnerNotFoundException("Partner not found: " + orderDTO.getPartnerId()));

        log.debug("Parsing dto to Order entity for partner: {}", partner.getId());
        Order order = new Order(partner, orderDTO);

        order.getItems().forEach(item -> item.setOrder(order));

        log.info("Calculating total value for order with items");
        log.debug("Items to be calculated: {}", order.getItems());
        order.calculateTotalValue();
        log.debug("Total value calculated: {}", order.getTotalValue());

        creditService.hasAvailableCredit(partner.getId(), order.getTotalValue());

        Order savedOrder = orderRepository.save(order);

        log.info("Order created successfully with ID: {}", savedOrder.getId());

        return new OrderResponseDTO(savedOrder);
    }

    /**
     * Retrieves an order by its ID.
     *
     * @param id the ID of the order
     * @return the order response
     * @throws OrderNotFoundException if the order does not exist
     */
    @Cacheable(value = "orders", key = "#id")
    public OrderResponseDTO getOrderById(UUID id) throws OrderNotFoundException {
        log.info("Fetching order by ID: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: %s".formatted(id)));

        return new OrderResponseDTO(order);
    }

    /**
     * Updates the status of an existing order.
     *
     * @param id the ID of the order
     * @param newStatus the new status to set
     * @return the updated order response
     * @throws OrderNotFoundException if the order does not exist
     */
    @Transactional
    @CacheEvict(value = "orders", key = "#id")
    public OrderResponseDTO updateOrderStatus(UUID id, Order.OrderStatus newStatus) throws OrderNotFoundException {
        log.info("Updating order {} status to {}", id, newStatus);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + id));

        Order.OrderStatus oldStatus = order.getStatus();

        OrderStatusStateMachine.transitionTo(order, newStatus);

        // if order is being approved, check credit
        if (newStatus == Order.OrderStatus.APPROVED && oldStatus == Order.OrderStatus.PENDING) {
            creditService.debitCredit(order.getPartner().getId(), order.getTotalValue());
        }

        // if order is being canceled, credit back the amount
        if (newStatus == Order.OrderStatus.CANCELED && oldStatus == Order.OrderStatus.APPROVED) {
            creditService.creditCredit(order.getPartner().getId(), order.getTotalValue());
        }


        log.info("Order {} status updated from {} to {}", id, oldStatus, newStatus);

        notificationService.notifyOrderStatusChange(order, oldStatus);

        return new OrderResponseDTO(order);
    }

    /**
     * Cancels an existing order.
     *
     * @param id the ID of the order to cancel
     * @throws OrderNotFoundException if the order does not exist
     */
    @Transactional
    @CacheEvict(value = "orders", key = "#id")
    public void cancelOrder(UUID id) throws OrderNotFoundException {
        log.info("Canceling order: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + id));
        Order.OrderStatus oldStatus = order.getStatus();
        order.cancel();
        log.info("Order {} status changed to CANCELED", id);
        notificationService.notifyOrderStatusChange(order, oldStatus);
    }

    public Page<OrderResponseDTO> searchOrders(OrderSearchDTO searchDTO) {
        log.info("Searching orders with criteria: {}", searchDTO);

        Page<Order> orders = orderRepository.findAll(OrderSpecifications.bySearchDto(searchDTO), searchDTO.toPageable());
        log.info("Found {} orders matching criteria", orders.getTotalElements());

        return orders.map(OrderResponseDTO::new);
    }
}
