package br.com.gazintech.orderapp.service;


import br.com.gazintech.orderapp.dto.OrderPostRequestDTO;
import br.com.gazintech.orderapp.dto.OrderResponseDTO;
import br.com.gazintech.orderapp.dto.OrderSearchDTO;
import br.com.gazintech.orderapp.entity.Order;
import br.com.gazintech.orderapp.entity.Partner;
import br.com.gazintech.orderapp.exception.OrderNotFoundException;
import br.com.gazintech.orderapp.exception.PartnerNotFoundException;
import br.com.gazintech.orderapp.notification.BroadcastSender;
import br.com.gazintech.orderapp.repository.OrderRepository;
import br.com.gazintech.orderapp.repository.PartnerRepository;
import br.com.gazintech.orderapp.utils.OrderStatusStateMachine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final PartnerRepository partnerRepository;
    private final CreditService creditService;
    private final BroadcastSender broadcastSender;

    @Transactional
    public OrderResponseDTO createOrder(OrderPostRequestDTO orderDTO) {
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
        broadcastSender.sendNotification(new OrderResponseDTO(order), "messageQueue");

        return new OrderResponseDTO(savedOrder);
    }

    @Cacheable(value = "orders", key = "#id")
    public OrderResponseDTO getOrderById(UUID id) {
        log.info("Fetching order by ID: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + id));

        return new OrderResponseDTO(order);
    }

    @Transactional
    @CacheEvict(value = "orders", key = "#id")
    public OrderResponseDTO updateOrderStatus(UUID id, Order.OrderStatus newStatus) {
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

        Order updatedOrder = orderRepository.save(order);

        log.info("Order {} status updated from {} to {}", id, oldStatus, newStatus);

//        notificationService.notifyOrderStatusChange(updatedOrder, oldStatus);

        return new OrderResponseDTO(updatedOrder);
    }

    //    public Page<OrderResponseDTO> searchOrders(OrderSearchDTO searchDTO) {
//        log.info("Searching orders with criteria: {}", searchDTO);
//
//        Pageable pageable = createPageable(searchDTO);
//        Page<Order> orders;
//
//
//
//        return orders.map(orderMapper::toDTO);
//    }
    @Transactional
    @CacheEvict(value = "orders", key = "#id")
    public void cancelOrder(UUID id) {
        log.info("Canceling order: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + id));
        order.cancel();
        log.info("Order {} status changed to CANCELED", id);
        orderRepository.save(order);
        broadcastSender.sendNotification(new OrderResponseDTO(order), "messageQueue");
    }

    public Page<OrderResponseDTO> searchOrders(OrderSearchDTO searchDTO) {
        log.info("Searching orders with criteria: {}", searchDTO);

        // Fetch the orders from the repository
        Page<Order> orders = orderRepository.searchOrders(searchDTO, searchDTO.toPageable());

        // Convert the Page<Order> to Page<OrderResponseDTO>
        return orders.map(OrderResponseDTO::new);
    }
}
