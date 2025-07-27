package br.com.gazintech.orderapp.dto;

import br.com.gazintech.orderapp.api.SortDirection;
import br.com.gazintech.orderapp.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderSearchDTO {

    private UUID orderId;

    private UUID partnerId;

    private Order.OrderStatus status;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Integer page = 0;

    private Integer size = 20;

    private String sortBy = "createdAt";

    private SortDirection sortDirection = SortDirection.DESC;

    public PageRequest toPageable() {
        return PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection.name()), sortBy));
    }
}
